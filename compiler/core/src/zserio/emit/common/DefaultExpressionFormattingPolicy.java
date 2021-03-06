package zserio.emit.common;

import zserio.ast.Expression;
import zserio.emit.common.ExpressionFormattingPolicy;

/**
 * Default formatting policy for emitters.
 *
 * This abstract policy implements operators which might be common for some emitters.
 */
public abstract class DefaultExpressionFormattingPolicy implements ExpressionFormattingPolicy
{
    @Override
    public UnaryExpressionFormatting getBigIntegerCastingToNative(Expression expr)
    {
        return new UnaryExpressionFormatting("");
    }

    @Override
    public UnaryExpressionFormatting getUnaryPlus(Expression expr)
    {
        return new UnaryExpressionFormatting("+");
    }

    @Override
    public UnaryExpressionFormatting getUnaryMinus(Expression expr)
    {
        return new UnaryExpressionFormatting("-");
    }

    @Override
    public UnaryExpressionFormatting getTilde(Expression expr)
    {
        return new UnaryExpressionFormatting("~");
    }

    @Override
    public UnaryExpressionFormatting getBang(Expression expr)
    {
        return new UnaryExpressionFormatting("!");
    }

    @Override
    public UnaryExpressionFormatting getLeftParenthesis(Expression expr)
    {
        return new UnaryExpressionFormatting("(", ")");
    }

    @Override
    public BinaryExpressionFormatting getComma(Expression expr)
    {
        return new BinaryExpressionFormatting(", ");
    }

    @Override
    public BinaryExpressionFormatting getLogicalOr(Expression expr)
    {
        return new BinaryExpressionFormatting(" || ");
    }

    @Override
    public BinaryExpressionFormatting getLogicalAnd(Expression expr)
    {
        return new BinaryExpressionFormatting(" && ");
    }

    @Override
    public BinaryExpressionFormatting getOr(Expression expr)
    {
        return new BinaryExpressionFormatting(" | ");
    }

    @Override
    public BinaryExpressionFormatting getXor(Expression expr)
    {
        return new BinaryExpressionFormatting(" ^ ");
    }

    @Override
    public BinaryExpressionFormatting getAnd(Expression expr)
    {
        return new BinaryExpressionFormatting(" & ");
    }

    @Override
    public BinaryExpressionFormatting getEq(Expression expr)
    {
        return new BinaryExpressionFormatting(" == ");
    }

    @Override
    public BinaryExpressionFormatting getNe(Expression expr)
    {
        return new BinaryExpressionFormatting(" != ");
    }

    @Override
    public BinaryExpressionFormatting getLt(Expression expr)
    {
        return new BinaryExpressionFormatting(" < ");
    }

    @Override
    public BinaryExpressionFormatting getLe(Expression expr)
    {
        return new BinaryExpressionFormatting(" <= ");
    }

    @Override
    public BinaryExpressionFormatting getGe(Expression expr)
    {
        return new BinaryExpressionFormatting(" >= ");
    }

    @Override
    public BinaryExpressionFormatting getGt(Expression expr)
    {
        return new BinaryExpressionFormatting(" > ");
    }

    @Override
    public BinaryExpressionFormatting getLeftShift(Expression expr)
    {
        return new BinaryExpressionFormatting(" << ");
    }

    @Override
    public BinaryExpressionFormatting getRightShift(Expression expr)
    {
        return new BinaryExpressionFormatting(" >> ");
    }

    @Override
    public BinaryExpressionFormatting getPlus(Expression expr)
    {
        return new BinaryExpressionFormatting(" + ");
    }

    @Override
    public BinaryExpressionFormatting getMinus(Expression expr)
    {
        return new BinaryExpressionFormatting(" - ");
    }

    @Override
    public BinaryExpressionFormatting getMultiply(Expression expr)
    {
        return new BinaryExpressionFormatting(" * ");
    }

    @Override
    public BinaryExpressionFormatting getDivide(Expression expr)
    {
        return new BinaryExpressionFormatting(" / ");
    }

    @Override
    public BinaryExpressionFormatting getModulo(Expression expr)
    {
        return new BinaryExpressionFormatting(" % ");
    }

    @Override
    public TernaryExpressionFormatting getQuestionMark(Expression expr)
    {
        return new TernaryExpressionFormatting(expr, "(", ") ? ", " : ", "");
    }
}
