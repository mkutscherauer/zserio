package zserio.ast;

import java.math.BigInteger;

/**
 * AST node for items defined by enumeration types.
 */
public class EnumItem extends DocumentableAstNode
{
    /**
     * Constructor.
     *
     * @param location        AST node location.
     * @param name            Name of the enumeration item.
     * @param valueExpression Expression value of the enumeration item.
     * @param docComment      Documentation comment belonging to this node.
     */
    public EnumItem(AstLocation location, String name, Expression valueExpression, DocComment docComment)
    {
        super(location, docComment);

        this.name = name;
        this.valueExpression = valueExpression;
    }

    @Override
    public void accept(ZserioAstVisitor visitor)
    {
        visitor.visitEnumItem(this);
    }

    @Override
    public void visitChildren(ZserioAstVisitor visitor)
    {
        super.visitChildren(visitor);

        if (valueExpression != null)
            valueExpression.accept(visitor);
    }

    /**
     * Gets the name of enumeration item.
     *
     * @return Returns name of enumeration item.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets enumeration item value expression.
     *
     * @return Enumeration item value expression or null if value expression has not been specified.
     */
    public Expression getValueExpression()
    {
        return valueExpression;
    }

    /**
     * Gets the integer value which represents the enumeration item.
     *
     * @return Returns the integer value of the enumeration item.
     */
    public BigInteger getValue()
    {
        return value;
    }

    /**
     * Sets the default integer value which represents the enumeration item.
     *
     * This method is called only if enumeration item value is not defined in the language.
     *
     * @param value Default integer value of the enumeration item.
     */
    void setValue(BigInteger value)
    {
        this.value = value;
    }

    /**
     * Evaluates enumeration item value expression.
     *
     * This method can be called from Expression.evaluate() method if some expression refers to enumeration
     * item before definition of this item. Therefore 'isEvaluated' check is necessary.
     */
    void evaluate()
    {
        if (!isEvaluated)
        {
            if (valueExpression != null)
            {
                if (valueExpression.getExprType() != Expression.ExpressionType.INTEGER)
                    throw new ParserException(valueExpression, "Enumeration item '" + getName() +
                            "' has non-integer value!");
                value = valueExpression.getIntegerValue();
            }

            isEvaluated = true;
        }
    }

    private final String name;
    private final Expression valueExpression;

    private BigInteger value = null;
    private boolean isEvaluated = false;
}
