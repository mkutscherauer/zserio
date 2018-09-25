package zserio.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import zserio.antlr.ZserioParserTokenTypes;
import zserio.antlr.util.BaseTokenAST;
import zserio.antlr.util.ParserException;
import zserio.tools.HashUtil;
import zserio.tools.ZserioToolPrinter;

/**
 * The representation of AST PACKAGE node.
 *
 * This class represents a Zserio package which provides a separate lexical scope for all type names.
 *
 * By default, only type names defined in the current package are visible. Names from other packages can be
 * made visible with an import declaration.
 */
public class Package extends TokenAST
{
    /**
     * Adds import to this package.
     *
     * This method is called from ANTLR TypeEvaluator walker.
     *
     * @param importedNode AST import node to add.
     */
    public void addImport(Import importedNode)
    {
        importedNodes.add(importedNode);
    }

    /**
     * Adds type reference to resolve.
     *
     * This method is called from ANTLR TypeEvaluator walker.
     *
     * @param typeReference Type reference to resolve.
     * @param scope         Scope which contains type reference to resolve or null if type reference is
     *                      directly in this package.
     */
    public void addTypeReferenceToResolve(TypeReference typeReference, Scope scope)
    {
        typeReferencesToResolve.put(typeReference, scope);
    }

    /**
     * Stores a type in the package types map.
     *
     * @param name AST node which defines type to add.
     * @param type Zserio type to add.
     *
     * @throws ParserException Throws if type has been already defined in the current package.
     */
    public void setLocalType(BaseTokenAST name, ZserioType type) throws ParserException
    {
        final ZserioType addedType = localTypes.put(name.getText(), type);
        if (addedType != null)
            throw new ParserException(name, "'" + name.getText() + "' is already defined in this package!");
    }

    /**
     * Resolves this package.
     *
     * This methods
     *
     * - resolves all imports which belong to this package
     * - resolves all type references which belong to this package
     * - resolves all subtypes which belong to this package
     *
     * @param packageNameMap   Map of all available package name to the package object.
     *
     * @throws ParserException In case of wrong import or wrong type reference or if cyclic subtype definition
     *                         is detected.
     */
    public void resolve(Map<PackageName, Package> packageNameMap) throws ParserException
    {
        // because subtypes can used type references, type references must be resolved before subtypes
        resolveImports(packageNameMap);
        resolveTypeReferences();
        resolveSubtypes();
    }

    /**
     * Gets name of the package.
     *
     * @return Package name.
     */
    public PackageName getPackageName()
    {
        return packageName;
    }

    /**
     * Gets Zserio type for given type name with its package if it's visible for this package.
     *
     * @param ownerToken      AST token which holds type to resolve (used for ParserException).
     * @param typePackageName Package name of the type to resolve.
     * @param typeName        Type name to resolve.
     *
     * @return Zserio type if given type name is visible for this package or null if given type name is unknown.
     *
     * @throws ParserException Throws in case of given type name is ambiguous for this package.
     */
    public ZserioType getVisibleType(BaseTokenAST ownerToken, PackageName typePackageName, String typeName)
            throws ParserException
    {
        ZserioType foundType = getLocalType(typePackageName, typeName);
        if (foundType == null)
        {
            foundType = getTypeFromImportedPackages(ownerToken, typePackageName, typeName);
            if (foundType == null)
                foundType = getTypeFromImportedSingleTypes(ownerToken, typePackageName, typeName);
        }

        return foundType;
    }

    /**
     * Gets list of all local types stored in the packages.
     *
     * This is called from doc emitter only. Doc emitter should be redesigned not to require such method. TODO
     *
     * @return List of all local types stored in the packages.
     */
    public Iterable<ZserioType> getLocalTypes()
    {
        return localTypes.values();
    }

    @Override
    protected boolean evaluateChild(BaseTokenAST child) throws ParserException
    {
        switch (child.getType())
        {
        case ZserioParserTokenTypes.ID:
            packageName.addId(child.getText());
            break;

        default:
            return false;
        }

        return true;
    }

    private void resolveImports(Map<PackageName, Package> packageNameMap) throws ParserException
    {
        for (Import importedNode : importedNodes)
        {
            final PackageName importedPackageName = importedNode.getImportedPackageName();
            final Package importedPackage = packageNameMap.get(importedPackageName);
            if (importedPackage == null)
                throw new ParserException(importedNode, "Default package cannot be imported!");

            final String importedTypeName = importedNode.getImportedTypeName();
            if (importedTypeName == null)
            {
                // this is package import
                if (importedPackages.contains(importedPackage))
                    ZserioToolPrinter.printWarning(importedNode, "Duplicated import of package '" +
                            importedPackageName.toString() + "'.");

                for (SingleTypeName importedSingleType : importedSingleTypes)
                {
                    if (importedSingleType.getPackageType().getPackageName().equals(importedPackageName))
                        ZserioToolPrinter.printWarning(importedNode, "Import of package '" +
                                importedPackageName.toString() + "' overwrites single type import '" +
                                importedSingleType.getTypeName() + "'.");
                }

                importedPackages.add(importedPackage);
            }
            else
            {
                // this is single type import
                if (importedPackages.contains(importedPackage))
                    ZserioToolPrinter.printWarning(importedNode, "Single type '" + importedTypeName +
                            "' imported already by package import.");

                final SingleTypeName importedSingleType = new SingleTypeName(importedPackage, importedTypeName);
                if (importedSingleTypes.contains(importedSingleType))
                    ZserioToolPrinter.printWarning(importedNode, "Duplicated import of type '" +
                            importedTypeName + "'.");

                final ZserioType importedZserioType = importedPackage.getLocalType(importedPackageName,
                        importedTypeName);
                if (importedZserioType == null)
                    throw new ParserException(importedNode, "Unknown type '" + importedTypeName +
                            "' in imported package '" + importedPackageName + "'!");

                importedSingleTypes.add(importedSingleType);
            }
        }
    }

    private void resolveTypeReferences() throws ParserException
    {
        for (Map.Entry<TypeReference, Scope> entry : typeReferencesToResolve.entrySet())
        {
            final Scope typeScope = entry.getValue();
            final ZserioType owner = (typeScope == null) ? null : typeScope.getOwner();
            entry.getKey().resolve(this, owner);
        }
    }

    private void resolveSubtypes() throws ParserException
    {
        for (ZserioType type : localTypes.values())
        {
            if (type instanceof Subtype)
                ((Subtype)type).resolve();
        }
    }

    private ZserioType getLocalType(PackageName typePackageName, String typeName)
    {
        if (!typePackageName.isEmpty() && !typePackageName.equals(packageName))
            return null;

        return localTypes.get(typeName);
    }

    private ZserioType getTypeFromImportedPackages(BaseTokenAST ownerToken, PackageName typePackageName,
            String typeName) throws ParserException
    {
        ZserioType foundType = null;
        for (Package importedPackage : importedPackages)
        {
            // don't exit the loop if something has been found, we need to check for ambiguities
            final ZserioType importedType = getImportedType(ownerToken, importedPackage, typePackageName,
                    typeName, foundType);
            if (importedType != null)
                foundType = importedType;
        }

        return foundType;
    }

    private ZserioType getTypeFromImportedSingleTypes(BaseTokenAST ownerToken, PackageName typePackageName,
            String typeName) throws ParserException
    {
        ZserioType foundType = null;
        for (SingleTypeName importedSingleType : importedSingleTypes)
        {
            // don't exit the loop if something has been found, we need to check for ambiguities
            if (typeName.equals(importedSingleType.getTypeName()))
            {
                final ZserioType importedType = getImportedType(ownerToken, importedSingleType.getPackageType(),
                        typePackageName, typeName, foundType);
                if (importedType != null)
                    foundType = importedType;
            }
        }

        return foundType;
    }

    private ZserioType getImportedType(BaseTokenAST ownerToken, Package importedPackage,
            PackageName typePackageName, String typeName, ZserioType foundType) throws ParserException
    {
        final ZserioType importedType = importedPackage.getLocalType(typePackageName, typeName);
        if (foundType != null)
        {
            // already found, we check for ambiguities
            if (importedType != null)
            {
                final String foundPackageName = foundType.getPackage().getPackageName().toString();
                final String importedTypePackageName = importedType.getPackage().getPackageName().toString();
                throw new ParserException(ownerToken, "Ambiguous type reference '" + typeName + "' found in " +
                        "packages '" + foundPackageName + "' and '" + importedTypePackageName + "'!");
            }
        }

        return importedType;
    }

    private static class SingleTypeName implements Comparable<SingleTypeName>, Serializable
    {
        public SingleTypeName(Package packageType, String typeName)
        {
            this.packageType = packageType;
            this.typeName = typeName;
        }

        @Override
        public int compareTo(SingleTypeName other)
        {
            final int result = typeName.compareTo(other.typeName);
            if (result != 0)
                return result;

            return packageType.getPackageName().compareTo(other.packageType.getPackageName());
        }

        @Override
        public boolean equals(Object other)
        {
            if (this == other)
                return true;

            if (other instanceof SingleTypeName)
                return compareTo((SingleTypeName)other) == 0;

            return false;
        }

        @Override
        public int hashCode()
        {
            int hash = HashUtil.HASH_SEED;
            hash = HashUtil.hash(hash, typeName);
            hash = HashUtil.hash(hash, packageType.getPackageName());

            return hash;
        }

        public Package getPackageType()
        {
            return packageType;
        }

        public String getTypeName()
        {
            return typeName;
        }

        private static final long serialVersionUID = -1L;

        private final Package packageType;
        private final String typeName;
    }

    private static final long serialVersionUID = -1L;

    private final PackageName packageName = new PackageName();
    private final Map<String, ZserioType> localTypes = new HashMap<String, ZserioType>();

    private final List<Import> importedNodes = new ArrayList<Import>();
    private final Set<Package> importedPackages = new HashSet<Package>();

    // this must be a TreeSet because of 'Ambiguous type reference' error checked in getVisibleType()
    private final Set<SingleTypeName> importedSingleTypes = new TreeSet<SingleTypeName>();

    // this must be a LinkedHashMap because of 'Circular containment' error checked during resolving of all
    // type references
    private final Map<TypeReference, Scope> typeReferencesToResolve = new LinkedHashMap<TypeReference, Scope>();
}
