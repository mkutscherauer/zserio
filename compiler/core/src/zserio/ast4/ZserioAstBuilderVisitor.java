package zserio.ast4;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import zserio.antlr.Zserio4Parser;
import zserio.antlr.Zserio4Parser.TypeArgumentContext;
import zserio.antlr.Zserio4ParserBaseVisitor;
import zserio.ast.PackageName;

public class ZserioAstBuilderVisitor extends Zserio4ParserBaseVisitor<Object>
{
    public ZserioAstBuilderVisitor(boolean checkUnusedTypes)
    {
        this.checkUnusedTypes = checkUnusedTypes;
    }

    public Root getAst()
    {
        return new Root(translationUnits, packageNameMap, checkUnusedTypes);
    }

    @Override
    public Object visitTranslationUnit(Zserio4Parser.TranslationUnitContext ctx)
    {
        // imports
        final List<Import> imports = new ArrayList<Import>();
        for (Zserio4Parser.ImportDeclarationContext importCtx : ctx.importDeclaration())
            imports.add((Import)visitImportDeclaration(importCtx));

        // package
        Package unitPackage = null;
        if (ctx.packageDeclaration() != null)
        {
            final PackageName packageName = (PackageName)visitPackageDeclaration(ctx.packageDeclaration());
            unitPackage = new Package(ctx.packageDeclaration().getStart(), packageName, imports);
        }
        else
        {
            unitPackage = new Package(ctx.getStart(), PackageName.EMPTY, imports); // default package
        }

        currentPackage = unitPackage; // set current package for types

        // types declarations
        final List<ZserioType> types = new ArrayList<ZserioType>();
        for (Zserio4Parser.TypeDeclarationContext typeCtx : ctx.typeDeclaration())
        {
            final Object typeDeclaration = visitTypeDeclaration(typeCtx);
            if (typeDeclaration != null) // TODO: remove null check when all types are implemented
                types.add((ZserioType)typeDeclaration);
        }

        currentPackage = null;

        final TranslationUnit translationUnit = new TranslationUnit(
                ctx.getStart(), unitPackage, imports, types);
        addTranslationUnit(translationUnit);

        return translationUnit;
    }

    @Override
    public Object visitPackageDeclaration(Zserio4Parser.PackageDeclarationContext ctx)
    {
        final PackageName packageName = createPackageName(ctx.qualifiedName().id());
        return packageName;
    }

    @Override
    public Object visitImportDeclaration(Zserio4Parser.ImportDeclarationContext ctx)
    {
        String importedTypeName = null;
        PackageName importedPackageName = null;

        if (ctx.MULTIPLY() == null)
        {
            importedPackageName = createPackageName(getPackageNameIds(ctx.id()));
            importedTypeName = getTypeNameId(ctx.id()).getText();
        }
        else
        {
            importedPackageName = createPackageName(ctx.id());
        }

        return new Import(ctx.getStart(), importedPackageName, importedTypeName);
    }

    @Override
    public Object visitConstDeclaration(Zserio4Parser.ConstDeclarationContext ctx)
    {
        return null; // TODO
    }

    @Override
    public Object visitSubtypeDeclaration(Zserio4Parser.SubtypeDeclarationContext ctx)
    {
        final String name = ctx.id().getText();
        final ZserioType targetType = (ZserioType)visitTypeName(ctx.typeName());

        final Subtype subtype = new Subtype(ctx.getStart(), currentPackage, name, targetType);
        currentPackage.setLocalType(subtype, ctx.id().getStart());
        return subtype;
    }

    @Override
    public Object visitStructureDeclaration(Zserio4Parser.StructureDeclarationContext ctx)
    {
        final String name = ctx.id().getText();

        final List<Parameter> parameters = new ArrayList<Parameter>();
        if (ctx.parameterList() != null)
        {
            for (Zserio4Parser.ParameterDefinitionContext parameterDefinitionCtx :
                    ctx.parameterList().parameterDefinition())
                parameters.add((Parameter)visitParameterDefinition(parameterDefinitionCtx));
        }

        final List<Field> fields = new ArrayList<Field>();
        for (Zserio4Parser.StructureFieldDefinitionContext fieldCtx : ctx.structureFieldDefinition())
            fields.add((Field)visitStructureFieldDefinition(fieldCtx));

        final List<FunctionType> functions = new ArrayList<FunctionType>();
        for (Zserio4Parser.FunctionDefinitionContext functionDefinitionCtx : ctx.functionDefinition())
            functions.add((FunctionType)visitFunctionDefinition(functionDefinitionCtx));

        final StructureType structureType = new StructureType(ctx.getStart(), currentPackage, name,
                parameters, fields, functions);
        currentPackage.setLocalType(structureType, ctx.id().getStart());
        return structureType;
    }

    @Override
    public Object visitStructureFieldDefinition(Zserio4Parser.StructureFieldDefinitionContext ctx)
    {
        final ZserioType type = getZserioType(ctx.fieldTypeId());
        final String name = ctx.fieldTypeId().id().getText();
        final boolean isAutoOptional = ctx.OPTIONAL() != null;

        final Expression alignmentExpr = ctx.fieldAlignment() != null
                ? (Expression)visitFieldAlignment(ctx.fieldAlignment()) : null;
        final Expression offsetExpr = ctx.fieldOffset() != null
                ? (Expression)visitFieldOffset(ctx.fieldOffset()) : null;
        final Expression initializerExpr = ctx.fieldInitializer() != null
                ? (Expression)visitFieldInitializer(ctx.fieldInitializer()) : null;
        final Expression optionalClauseExpr = ctx.fieldOptionalClause() != null
                ? (Expression)visitFieldOptionalClause(ctx.fieldOptionalClause()) : null;
        final Expression constraintExpr = ctx.fieldConstraint() != null
                ? (Expression)visitFieldConstraint(ctx.fieldConstraint()) : null;

        return new Field(ctx.getStart(), type, name, isAutoOptional, alignmentExpr, offsetExpr, initializerExpr,
                optionalClauseExpr, constraintExpr);
    }

    private ZserioType getZserioType(Zserio4Parser.FieldTypeIdContext ctx)
    {
        final ZserioType type = (ZserioType)visitTypeReference(ctx.typeReference());
        if (ctx.fieldArrayRange() == null)
            return type;

        final Expression lengthExpression = (Expression)visit(ctx.fieldArrayRange().expression());
        return new ArrayType(ctx.getStart(), type, lengthExpression, ctx.IMPLICIT() != null);
    }

    @Override
    public Object visitFieldAlignment(Zserio4Parser.FieldAlignmentContext ctx)
    {
        return new Expression(ctx.DECIMAL_LITERAL().getSymbol());
    }

    @Override
    public Object visitFunctionDefinition(Zserio4Parser.FunctionDefinitionContext ctx)
    {
        final ZserioType returnType = (ZserioType)visitTypeName(ctx.functionType().typeName());
        final String name = ctx.functionName().getText();
        final Expression resultExpression = (Expression)visit(ctx.functionBody().expression());

        return new FunctionType(ctx.getStart(), currentPackage, returnType, name, resultExpression);
    }

    @Override
    public Object visitParameterDefinition(Zserio4Parser.ParameterDefinitionContext ctx)
    {
        return new Parameter(ctx.getStart(), (ZserioType)visitTypeName(ctx.typeName()), ctx.id().getText());
    }

    @Override
    public Object visitParenthesizedExpression(Zserio4Parser.ParenthesizedExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), ctx.operator, operand1);
    }

    @Override
    public Object visitFunctionCallExpression(Zserio4Parser.FunctionCallExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), ctx.operator, operand1);
    }

    @Override
    public Object visitArrayExpression(Zserio4Parser.ArrayExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitDotExpression(Zserio4Parser.DotExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        final Expression operand2 = new Expression(ctx.id().ID().getSymbol());
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitLengthofExpression(Zserio4Parser.LengthofExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), ctx.operator, operand1);
    }

    @Override
    public Object visitSumExpression(Zserio4Parser.SumExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), ctx.operator, operand1);
    }

    @Override
    public Object visitValueofExpression(Zserio4Parser.ValueofExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), ctx.operator, operand1);
    }

    @Override
    public Object visitNumbitsExpression(Zserio4Parser.NumbitsExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), ctx.operator, operand1);
    }

    @Override
    public Object visitUnaryExpression(Zserio4Parser.UnaryExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), ctx.operator, operand1);
    }

    @Override
    public Object visitMultiplicativeExpression(Zserio4Parser.MultiplicativeExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitAdditiveExpression(Zserio4Parser.AdditiveExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitShiftExpression(Zserio4Parser.ShiftExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitRelationalExpression(Zserio4Parser.RelationalExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitEqualityExpression(Zserio4Parser.EqualityExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitBitwiseAndExpression(Zserio4Parser.BitwiseAndExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitBitwiseXorExpression(Zserio4Parser.BitwiseXorExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitBitwiseOrExpression(Zserio4Parser.BitwiseOrExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitLogicalAndExpression(Zserio4Parser.LogicalAndExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitLogicalOrExpression(Zserio4Parser.LogicalOrExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitTernaryExpression(Zserio4Parser.TernaryExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        final Expression operand3 = (Expression)visit(ctx.expression(2));
        return new Expression(ctx.getStart(), ctx.operator, operand1, operand2, operand3);
    }

    @Override
    public Object visitLiteralExpression(Zserio4Parser.LiteralExpressionContext ctx)
    {
        return new Expression(ctx.literal().getStart());
    }

    @Override
    public Object visitIndexExpression(Zserio4Parser.IndexExpressionContext ctx)
    {
        return new Expression(ctx.INDEX().getSymbol());
    }

    @Override
    public Object visitIdentifierExpression(Zserio4Parser.IdentifierExpressionContext ctx)
    {
        return new Expression(ctx.id().ID().getSymbol());
    }

    @Override
    public Object visitTypeName(Zserio4Parser.TypeNameContext ctx)
    {
        if (ctx.builtinType() != null)
            return visitBuiltinType(ctx.builtinType());

        final PackageName referencedPackageName = createPackageName(
                getPackageNameIds(ctx.qualifiedName().id()));
        final String referencedTypeName = getTypeNameId(ctx.qualifiedName().id()).getText();
        final boolean isParameterized = false;
        final TypeReference typeReference =
                new TypeReference(ctx.getStart(), referencedPackageName, referencedTypeName, isParameterized);

        currentPackage.addTypeReferenceToResolve(typeReference);
        return typeReference;
    }

    @Override
    public Object visitTypeReference(Zserio4Parser.TypeReferenceContext ctx)
    {
        if (ctx.builtinType() != null)
            return visitBuiltinType(ctx.builtinType());

        final PackageName referencedPackageName = createPackageName(
                getPackageNameIds(ctx.qualifiedName().id()));
        final String referencedTypeName = getTypeNameId(ctx.qualifiedName().id()).getText();
        final boolean isParameterized = ctx.typeArgumentList() != null;
        final TypeReference typeReference =
                new TypeReference(ctx.getStart(), referencedPackageName, referencedTypeName, isParameterized);
        currentPackage.addTypeReferenceToResolve(typeReference);

        if (isParameterized)
        {
            final List<Expression> arguments = new ArrayList<Expression>();
            for (TypeArgumentContext typeArgumentCtx : ctx.typeArgumentList().typeArgument())
                arguments.add((Expression)visitTypeArgument(typeArgumentCtx));
            return new TypeInstantiation(ctx.getStart(), typeReference, arguments);
        }
        else
        {
            return typeReference;
        }
    }

    @Override
    public Object visitTypeArgument(Zserio4Parser.TypeArgumentContext ctx)
    {
        if (ctx.EXPLICIT() != null)
            return new Expression(ctx.getStart(), ctx.id().ID().getSymbol(), true);
        else
            return visit(ctx.expression());
    }

    @Override
    public Object visitIntType(Zserio4Parser.IntTypeContext ctx)
    {
        return new StdIntegerType(ctx.getStart());
    }

    @Override
    public Object visitVarintType(Zserio4Parser.VarintTypeContext ctx)
    {
        return new VarIntegerType(ctx.getStart());
    }

    @Override
    public Object visitUnsignedBitFieldType(Zserio4Parser.UnsignedBitFieldTypeContext ctx)
    {
        // final Expression expression = ctx.bitfieldLength();
        // TODO: use length expression
        return new UnsignedBitFieldType(ctx.getStart());
    }

    @Override
    public Object visitSignedBitFieldType(Zserio4Parser.SignedBitFieldTypeContext ctx)
    {
        // TODO: use length expression
        return new SignedBitFieldType(ctx.getStart());
    }

    @Override
    public Object visitBoolType(Zserio4Parser.BoolTypeContext ctx)
    {
        return new BooleanType(ctx.getStart());
    }

    @Override
    public Object visitStringType(Zserio4Parser.StringTypeContext ctx)
    {
        return new StringType(ctx.getStart());
    }

    @Override
    public Object visitFloatType(Zserio4Parser.FloatTypeContext ctx)
    {
        return new FloatType(ctx.getStart());
    }

    /**
     * Adds translation unit to this root node.
     *
     * @param translationUnit Translation unit to add.
     */
    private void addTranslationUnit(TranslationUnit translationUnit)
    {
        translationUnits.add(translationUnit);

        final Package unitPackage = translationUnit.getPackage();
        if (packageNameMap.put(unitPackage.getPackageName(), unitPackage) != null)
        {
            // translation unit package already exists, this could happen only for default packages
            throw new ParserException(translationUnit, "Multiple default packages are not allowed!");
        }
    }

    private PackageName createPackageName(List<Zserio4Parser.IdContext> ids)
    {
        final PackageName.Builder packageNameBuilder = new PackageName.Builder();
        for (Zserio4Parser.IdContext id : ids)
            packageNameBuilder.addId(id.getText());
        return packageNameBuilder.get();
    }

    private List<Zserio4Parser.IdContext> getPackageNameIds(List<Zserio4Parser.IdContext> qualifiedName)
    {
        return qualifiedName.subList(0, qualifiedName.size() - 1);
    }

    private Zserio4Parser.IdContext getTypeNameId(List<Zserio4Parser.IdContext> qualifiedName)
    {
        return qualifiedName.get(qualifiedName.size() - 1);
    }

    private final boolean checkUnusedTypes;
    private final List<TranslationUnit> translationUnits = new ArrayList<TranslationUnit>();
    private final Map<PackageName, Package> packageNameMap = new LinkedHashMap<PackageName, Package>();

    private Package currentPackage = null;
}
