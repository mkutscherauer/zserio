package zserio.emit.python;

import zserio.ast.Subtype;
import zserio.emit.common.ZserioEmitException;
import zserio.emit.python.types.PythonNativeType;

public class SubtypeEmitterTemplateData extends PythonTemplateData
{
    public SubtypeEmitterTemplateData(TemplateDataContext context, Subtype subtype)
            throws ZserioEmitException
    {
        super(context);

        name = subtype.getName();

        final PythonNativeMapper pythonNativeMapper = context.getPythonNativeMapper();
        final PythonNativeType nativeTargetType =
                pythonNativeMapper.getPythonType(subtype.getTypeReference());
        importUsedType(nativeTargetType);
        targetTypeName = nativeTargetType.getFullName();
    }

    public String getName()
    {
        return name;
    }

    public String getTargetTypeName()
    {
        return targetTypeName;
    }

    private final String name;
    private final String targetTypeName;
}
