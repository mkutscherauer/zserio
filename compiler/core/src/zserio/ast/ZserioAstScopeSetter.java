package zserio.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ZserioAstVisitor which handles scopes of symbols.
 */
public class ZserioAstScopeSetter extends ZserioAstWalker
{
    @Override
    public void visitStructureType(StructureType structureType)
    {
        currentScope = structureType.getScope();
        fillExpressionScopes = true;

        structureType.visitChildren(this);

        currentScope = defaultScope;
        expressionScopes.clear();
        fillExpressionScopes = false;

        visitInstantiations(structureType);
    }

    @Override
    public void visitField(Field field)
    {
        field.getTypeInstantiation().accept(this);

        if (field.getAlignmentExpr() != null)
            field.getAlignmentExpr().accept(this);

        if (field.getOffsetExpr() != null)
            field.getOffsetExpr().accept(this);

        if (field.getInitializerExpr() != null)
            field.getInitializerExpr().accept(this);

        if (field.getOptionalClauseExpr() != null)
            field.getOptionalClauseExpr().accept(this);

        currentScope.setSymbol(field.getName(), field);

        if (field.getConstraintExpr() != null)
            field.getConstraintExpr().accept(this);

        if (field.getSqlConstraint() != null)
            field.getSqlConstraint().accept(this);
    }

    @Override
    public void visitChoiceType(ChoiceType choiceType)
    {
        currentScope = choiceType.getScope();
        fillExpressionScopes = true;

        for (Parameter parameter : choiceType.getTypeParameters())
            parameter.accept(this);

        currentChoiceOrUnionScope = currentScope;
        currentScope = new Scope(currentChoiceOrUnionScope);

        choiceType.getSelectorExpression().accept(this);

        for (ChoiceCase choiceCase : choiceType.getChoiceCases())
            choiceCase.accept(this);

        if (choiceType.getChoiceDefault() != null)
            choiceType.getChoiceDefault().accept(this);

        currentScope = currentChoiceOrUnionScope;
        currentChoiceOrUnionScope = null;

        for (Function function : choiceType.getFunctions())
            function.accept(this);

        currentScope = defaultScope;
        expressionScopes.clear();
        fillExpressionScopes = false;

        visitInstantiations(choiceType);
    }

    @Override
    public void visitChoiceCase(ChoiceCase choiceCase)
    {
        for (ChoiceCaseExpression caseExpression : choiceCase.getExpressions())
            caseExpression.accept(this);

        if (choiceCase.getField() != null)
            visitChoiceField(choiceCase.getField());
    }

    @Override
    public void visitChoiceDefault(ChoiceDefault choiceDefault)
    {
        if (choiceDefault.getField() != null)
            visitChoiceField(choiceDefault.getField());
    }

    @Override
    public void visitUnionType(UnionType unionType)
    {
        currentScope = unionType.getScope();
        fillExpressionScopes = true;

        for (Parameter parameter : unionType.getTypeParameters())
            parameter.accept(this);

        currentChoiceOrUnionScope = currentScope;
        currentScope = new Scope(currentChoiceOrUnionScope);

        for (Field field : unionType.getFields())
            visitChoiceField(field);

        currentScope = currentChoiceOrUnionScope;
        currentChoiceOrUnionScope = null;

        for (Function function : unionType.getFunctions())
            visitFunction(function);

        currentScope = defaultScope;
        expressionScopes.clear();
        fillExpressionScopes = false;

        visitInstantiations(unionType);
    }

    @Override
    public void visitEnumType(EnumType enumType)
    {
        currentScope = enumType.getScope();

        enumType.visitChildren(this);

        currentScope = defaultScope;
    }

    @Override
    public void visitEnumItem(EnumItem enumItem)
    {
        enumItem.visitChildren(this);
        currentScope.setSymbol(enumItem.getName(), enumItem);
    }

    @Override
    public void visitBitmaskType(BitmaskType bitmaskType)
    {
        currentScope = bitmaskType.getScope();

        bitmaskType.visitChildren(this);

        currentScope = defaultScope;
    }

    @Override
    public void visitBitmaskValue(BitmaskValue bitmaskValue)
    {
        bitmaskValue.visitChildren(this);
        currentScope.setSymbol(bitmaskValue.getName(), bitmaskValue);
    }

    @Override
    public void visitSqlTableType(SqlTableType sqlTableType)
    {
        currentScope = sqlTableType.getScope();

        sqlTableType.visitChildren(this);

        currentScope = defaultScope;

        visitInstantiations(sqlTableType);
    }

    @Override
    public void visitSqlDatabaseType(SqlDatabaseType sqlDatabaseType)
    {
        currentScope = sqlDatabaseType.getScope();

        sqlDatabaseType.visitChildren(this);

        currentScope = defaultScope;
    }

    @Override
    public void visitServiceType(ServiceType serviceType)
    {
        currentScope = serviceType.getScope();

        serviceType.visitChildren(this);

        currentScope = defaultScope;
    }

    @Override
    public void visitServiceMethod(ServiceMethod serviceMethod)
    {
        serviceMethod.visitChildren(this);

        currentScope.setSymbol(serviceMethod.getName(), serviceMethod);
    }

    @Override
    public void visitPubsubType(PubsubType pubsubType)
    {
        currentScope = pubsubType.getScope();

        pubsubType.visitChildren(this);

        currentScope = defaultScope;
    }

    @Override
    public void visitPubsubMessage(PubsubMessage pubsubMessage)
    {
        pubsubMessage.visitChildren(this);

        currentScope.setSymbol(pubsubMessage.getName(), pubsubMessage);
    }

    @Override
    public void visitFunction(Function function)
    {
        for (Scope expressionScope : expressionScopes)
            expressionScope.setSymbol(function.getName(), function);

        function.visitChildren(this);

        currentScope.setSymbol(function.getName(), function);
    }

    @Override
    public void visitParameter(Parameter parameter)
    {
        parameter.visitChildren(this);

        currentScope.setSymbol(parameter.getName(), parameter);
    }

    @Override
    public void visitExpression(Expression expression)
    {
        expression.visitChildren(this);

        final Scope expressionScope = new Scope(currentScope);
        if (fillExpressionScopes)
            expressionScopes.add(expressionScope);
        expression.setEvaluationScope(expressionScope);
    }

    @Override
    public void visitArrayType(ArrayType arrayType)
    {
        arrayType.visitChildren(this);
    }

    @Override
    public void visitTemplateParameter(TemplateParameter templateParameter)
    {
        templateParameter.visitChildren(this);
        currentScope.setSymbol(templateParameter.getName(), templateParameter);
    }

    private void visitChoiceField(Field field)
    {
        field.getTypeInstantiation().accept(this);

        currentScope.setSymbol(field.getName(), field);
        currentChoiceOrUnionScope.setSymbol(field.getName(), field);

        if (field.getConstraintExpr() != null)
            field.getConstraintExpr().accept(this);

        currentScope.removeSymbol(field.getName());
    }

    private void visitInstantiations(ZserioTemplatableType templatable)
    {
        for (ZserioTemplatableType instantiation : templatable.getInstantiations())
        {
            try
            {
                instantiation.accept(this);
            }
            catch (ParserException e)
            {
                // This should never happen since scope errors are caught directly in the template declaration.
                throw new InstantiationException(e, instantiation.getInstantiationReferenceStack());
            }
        }
    }

    private final Scope defaultScope = new Scope((ZserioScopedType)null);
    private Scope currentScope = defaultScope;
    private Scope currentChoiceOrUnionScope = null;

    private final List<Scope> expressionScopes = new ArrayList<Scope>();
    private boolean fillExpressionScopes = false;
}
