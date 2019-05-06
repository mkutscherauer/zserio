package zserio.emit.common.sql;

import zserio.ast4.ArrayType;
import zserio.ast4.BitFieldType;
import zserio.ast4.BooleanType;
import zserio.ast4.ChoiceType;
import zserio.ast4.ConstType;
import zserio.ast4.ZserioType;
import zserio.ast4.ZserioTypeUtil;
import zserio.ast4.EnumType;
import zserio.ast4.FloatType;
import zserio.ast4.FunctionType;
import zserio.ast4.ServiceType;
import zserio.ast4.StructureType;
import zserio.ast4.SqlDatabaseType;
import zserio.ast4.SqlTableType;
import zserio.ast4.StdIntegerType;
import zserio.ast4.StringType;
import zserio.ast4.Subtype;
import zserio.ast4.TypeInstantiation;
import zserio.ast4.TypeReference;
import zserio.ast4.UnionType;
import zserio.ast4.VarIntegerType;
import zserio.ast4.ZserioAstVisitorBase;
import zserio.emit.common.ZserioEmitException;
import zserio.emit.common.sql.types.NativeBlobType;
import zserio.emit.common.sql.types.NativeIntegerType;
import zserio.emit.common.sql.types.NativeRealType;
import zserio.emit.common.sql.types.NativeTextType;
import zserio.emit.common.sql.types.SqlNativeType;

/**
 * Mapper from Zserio types to SQLite3 types.
 *
 * For the types supported by SQLite, see https://www.sqlite.org/datatype3.html
 */
public class SqlNativeTypeMapper
{
    /**
     * Gets SQLite3 native type from Zserio type.
     *
     * @param type Zserio type to map to SQLite3 native type.
     *
     * @return Mapped Zserio type to SQLite3 native type.
     *
     * @throws ZserioEmitException Throws in case of internal error.
     */
    public SqlNativeType getSqlType(ZserioType type) throws ZserioEmitException
    {
        // resolve all the way through subtypes to the base type
        type = TypeReference.resolveBaseType(type);

        final TypeMapperVisitor visitor = new TypeMapperVisitor();
        type.accept(visitor);
        final SqlNativeType nativeType = visitor.getSqlType();

        if (nativeType == null)
            throw new ZserioEmitException("Unexpected element '" + ZserioTypeUtil.getFullName(type) +
                    "' of type '" + type.getClass() + "' in SqlNativeTypeMapper!");

        return nativeType;
    }

    private static class TypeMapperVisitor extends ZserioAstVisitorBase
    {
        SqlNativeType getSqlType()
        {
            return sqlType;
        }

        @Override
        public void visitArrayType(ArrayType type)
        {
            // not supported
        }

        @Override
        public void visitBooleanType(BooleanType type)
        {
            sqlType = integerType;
        }

        @Override
        public void visitChoiceType(ChoiceType type)
        {
            sqlType = blobType;
        }

        @Override
        public void visitConstType(ConstType type)
        {
            // not supported
        }

        @Override
        public void visitEnumType(EnumType type)
        {
            sqlType = integerType;
        }

        @Override
        public void visitFloatType(FloatType type)
        {
            sqlType = realType;
        }

        @Override
        public void visitFunction(FunctionType type)
        {
            // not supported
        }

        @Override
        public void visitServiceType(ServiceType type)
        {
            // not supported
        }

        @Override
        public void visitBitFieldType(BitFieldType type)
        {
            sqlType = integerType;
        }

        @Override
        public void visitSqlDatabaseType(SqlDatabaseType type)
        {
            // not supported
        }

        @Override
        public void visitSqlTableType(SqlTableType type)
        {
            // not supported
        }

        @Override
        public void visitStdIntegerType(StdIntegerType type)
        {
            sqlType = integerType;
        }

        @Override
        public void visitStringType(StringType type)
        {
            sqlType = textType;
        }

        @Override
        public void visitStructureType(StructureType type)
        {
            sqlType = blobType;
        }

        @Override
        public void visitSubtype(Subtype type)
        {
            // not supported
        }

        @Override
        public void visitTypeInstantiation(TypeInstantiation type)
        {
            sqlType = blobType;
        }

        @Override
        public void visitTypeReference(TypeReference type)
        {
            // not supported
        }

        @Override
        public void visitUnionType(UnionType type)
        {
            sqlType = blobType;
        }

        @Override
        public void visitVarIntegerType(VarIntegerType type)
        {
            sqlType = integerType;
        }

        private SqlNativeType sqlType = null;
    }

    private final static NativeIntegerType integerType = new NativeIntegerType();
    private final static NativeRealType realType = new NativeRealType();
    private final static NativeTextType textType = new NativeTextType();
    private final static NativeBlobType blobType = new NativeBlobType();
}
