package zserio.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import zserio.antlr.ZserioParserTokenTypes;
import zserio.antlr.util.BaseTokenAST;
import zserio.antlr.util.ParserException;

/**
 * AST node for parametrized type instantiations.
 *
 * Parametrized type instantiations are type references to parametrized types with given arguments. All others
 * type references are modelled by AST node TypeReference.
 *
 * Parametrized type instantiations are Zserio types as well.
 */
public class TypeInstantiation extends TokenAST implements ZserioType
{
    @Override
    public Package getPackage()
    {
        return baseType.getPackage();
    }

    @Override
    public String getName()
    {
        return referencedType.getName() + "()";
    }

    @Override
    public Iterable<ZserioType> getUsedTypeList()
    {
        throw new InternalError("TypeInstantiation.getUsedTypeList() is not implemented!");
    }

    @Override
    public void callVisitor(ZserioTypeVisitor visitor)
    {
        visitor.visitTypeInstantiation(this);
    }

    /**
     * Evaluates base type of this type instantiation.
     *
     * This method is called from code generated by ANTLR using ExpressionEvaluator.g grammar as soon as type
     * instantiation is evaluated.
     *
     * This method can be called directly from Expression.evaluate() method if some expression refers to
     * the type instantiation before its definition.
     *
     * @throws ParserException Throws if parametrized type instantiation does not refer to a compound type.
     */
    public void evaluateBaseType() throws ParserException
    {
        if (baseType == null)
        {
            // check if referenced type is a compound type
            final ZserioType resolvedReferencedType = TypeReference.resolveBaseType(referencedType);
            if (!(resolvedReferencedType instanceof CompoundType))
                throw new ParserException(referencedType, "Parametrized type instantiation '" + getName() +
                        "' does not refer to a compound type!");
            baseType = (CompoundType)resolvedReferencedType;

            // check if referenced type is a parametrized type
            final List<Parameter> parameters = baseType.getParameters();
            final int numParameters = parameters.size();
            if (numParameters == 0)
                throw new ParserException(referencedType, "Parametrized type instantiation '" + getName() +
                        "' does not refer to a parameterized type!");
            if (arguments.size() != numParameters)
                throw new ParserException(referencedType, "Parametrized type instantiation '" + getName() +
                        "' has wrong number of arguments (" + arguments.size() + " != " + numParameters + ")!");

            // fill instantiated parameter list
            for (int i = 0; i < numParameters; i++)
                instantiatedParameters.add(new InstantiatedParameter(arguments.get(i), parameters.get(i)));
        }
    }

    /**
     * Gets the type to which this type instantiation refers.
     *
     * @return Type referenced by this instantiation.
     */
    public ZserioType getReferencedType()
    {
        return referencedType;
    }

    /**
     * Gets the compound type to which this type instantiation refers.
     *
     * @return Base type of this type instantiation.
     */
    public CompoundType getBaseType()
    {
        return baseType;
    }

    /**
     * Gets a list of parameters used in this type instantiation.
     *
     * This returns a list of parameters of the base type together with the expressions used in the
     * instantiation.
     *
     * The order of the items in list is the order of the parameters.
     *
     * @return The list of instantiated parameters.
     */
    public List<InstantiatedParameter> getInstantiatedParameters()
    {
        return instantiatedParameters;
    }

    /**
     * Class describing one parameter in a type instantiation.
     */
    public static class InstantiatedParameter implements Serializable
    {
        /**
         * Constructor from argument expression and parameter.
         *
         * @param argumentexpression Argument expression used for parameter instantiation.
         * @param parameter          The parameter as used in the definition of the parameterized compound type.
         */
        public InstantiatedParameter(Expression argumentExpression, Parameter parameter)
        {
            this.argumentExpression = argumentExpression;
            this.parameter = parameter;
        }

        /**
         * Gets the argument expression.
         *
         * @return The expression used for parameter instantiation.
         */
        public Expression getArgumentExpression()
        {
            return argumentExpression;
        }

        /**
         * Gets the parameter as used in the definition of the parametrized compound type.
         *
         * @return The parameter that is instantiated.
         */
        public Parameter getParameter()
        {
            return parameter;
        }

        private static final long serialVersionUID = 8311435045636275517L;

        private final Expression argumentExpression;
        private final Parameter parameter;
    }

    @Override
    protected boolean evaluateChild(BaseTokenAST child) throws ParserException
    {
        switch (child.getType())
        {
        case ZserioParserTokenTypes.TYPEREF:
            if (!(child instanceof TypeReference))
                return false;
            referencedType = (TypeReference)child;
            break;

        default:
            if (!(child instanceof Expression))
                return false;
            arguments.add((Expression)child);
            break;
        }

        return true;
    }

    @Override
    protected void check() throws ParserException
    {
        // check all argument types in instantiated parameter list
        for (InstantiatedParameter instantiatedParameter : instantiatedParameters)
        {
            final Expression argumentExpression = instantiatedParameter.getArgumentExpression();
            if (!argumentExpression.isExplicitVariable())
            {
                ExpressionUtil.checkExpressionType(argumentExpression, TypeReference.resolveBaseType(
                        instantiatedParameter.getParameter().getParameterType()));
            }
        }
    }

    private static final long serialVersionUID = -7316936170722028250L;

    private TypeReference referencedType;
    private final List<Expression> arguments = new ArrayList<Expression>();

    private CompoundType baseType;
    private final List<InstantiatedParameter> instantiatedParameters = new ArrayList<InstantiatedParameter>();
}
