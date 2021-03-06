package zserio.ast;

/**
 * The class represents symbol reference.
 *
 * Symbol reference can be for example
 *
 * - reference to constant ('packageName.FooConstant')
 * - reference to enumeration item ('packageName.FooEnumeration.BAR_ENUM_ITEM')
 * - reference to compound field ('packageName.FooCompoundType.field')
 *
 * Symbol reference is not AST node because it is used by unparsed texts in the following two situations:
 *
 * - documentation comment tag see token (@see)
 * - SQLite constraints (@expression)
 */
public class SymbolReference
{
    /**
     * Constructor from owner token and text.
     *
     * @param ownerNode           AST token which owns the symbol reference test.
     * @param symbolReferenceText Symbol reference in unparsed text format.
     */
    public SymbolReference(AstNode ownerNode, String symbolReferenceText)
    {
        this.ownerNode = ownerNode;

        final String[] referenceElementList = symbolReferenceText.split("\\" + SYMBOL_REFERENCE_SEPARATOR);
        for (String referenceElement : referenceElementList)
        {
            if (referencedName != null)
                referencedPackageNameBuilder.addId(referencedName);
            referencedName = referenceElement;
        }
    }

    /**
     * Gets referenced type.
     *
     * @return Referenced type or null if it's a global symbol.
     */
    public ZserioType getReferencedType()
    {
        return referencedType;
    }

    /**
     * Gets referenced symbol.
     *
     * @return Referenced enumeration item or compound field or null if this is a type reference.
     */
    public AstNode getReferencedSymbol()
    {
        return referencedSymbol;
    }

    /**
     * Gets referenced symbol name.
     *
     * @return Referenced enumeration item or compound field in string format or null if this is a type
     *         reference.
     */
    public String getReferencedSymbolName()
    {
        return referencedSymbolName;
    }

    /**
     * Resolves the symbol reference.
     *
     * @param ownerPackage Zserio package in which the symbol reference is defined.
     * @param ownerType ZserioType which is owner of the symbol reference.
     */
    void resolve(Package ownerPackage, ZserioScopedType ownerType)
    {
        // try if the last link component was a symbol name
        referencedSymbol = ownerPackage.getVisibleSymbol(ownerNode,  referencedPackageNameBuilder.get(),
                referencedName);
        if (referencedSymbol != null)
        {
            referencedSymbolName = referencedName;
            return;
        }

        // try if the last link component was a type name
        referencedType = ownerPackage.getVisibleType(ownerNode, referencedPackageNameBuilder.get(),
                referencedName);
        if (referencedType == null)
        {
            final String fullName = ZserioTypeUtil.getFullName(referencedPackageNameBuilder.get(),
                    referencedName);

            // try if the last link component was not type name (can be a field name or enumeration item)
            referencedSymbolName = referencedName;
            referencedName = referencedPackageNameBuilder.removeLastId();
            if (referencedName == null)
            {
                // there is only symbol name, try to resolve it in owner scope
                referencedType = ownerType;
            }
            else
            {
                referencedType = ownerPackage.getVisibleType(ownerNode, referencedPackageNameBuilder.get(),
                        referencedName);
            }

            // this was our last attempt to resolve symbol type
            if (referencedType == null)
                throw new ParserException(ownerNode, "Unresolved referenced symbol '" + fullName + "'!");

            resolveLocalSymbol();
        }
    }

    private void resolveLocalSymbol()
    {
        if (!(referencedType instanceof ZserioScopedType))
            throw new ParserException(ownerNode, "Referenced type '" + referencedType.getName() +
                    "' can't refer to '" + referencedSymbolName + "'!");

        final Scope referencedScope = ((ZserioScopedType)referencedType).getScope();
        referencedSymbol = referencedScope.getSymbol(referencedSymbolName);
        if (referencedSymbol == null)
            throw new ParserException(ownerNode, "Unresolved referenced symbol '" + referencedSymbolName +
                    "' for type '" + referencedType.getName() + "'!");
    }

    private static final String SYMBOL_REFERENCE_SEPARATOR = ".";

    private final AstNode ownerNode;
    private final PackageName.Builder referencedPackageNameBuilder = new PackageName.Builder();
    private String referencedName = null;
    private String referencedSymbolName = null;
    private ZserioType referencedType = null;
    private AstNode referencedSymbol = null;
}
