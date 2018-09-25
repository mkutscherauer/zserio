package zserio.emit.cpp;

import java.util.ArrayList;
import java.util.List;

import antlr.collections.AST;
import zserio.ast.SqlTableType;
import zserio.tools.Parameters;

public class InspectorParameterProviderEmitter extends CppDefaultEmitter
{
    public InspectorParameterProviderEmitter(String outPathName, Parameters extensionParameters)
    {
        super(outPathName, extensionParameters);
    }

    @Override
    public void beginSqlTable(AST token) throws ZserioEmitCppException
    {
        if (!(token instanceof SqlTableType))
            throw new ZserioEmitCppException("Unexpected token type in beginSqlTable!");

        if (getWithSqlCode() && getWithInspectorCode())
            sqlTableTypes.add((SqlTableType)token);
    }

    @Override
    public void endRoot() throws ZserioEmitCppException
    {

        if (!sqlTableTypes.isEmpty())
        {
            final ParameterProviderTemplateData templateData =
                    new ParameterProviderTemplateData(getTemplateDataContext(), sqlTableTypes);

            processHeaderTemplateToRootDir(IINSPECTOR_TEMPLATE_HEADER_NAME, templateData,
                    IINSPECTOR_OUTPUT_FILE_NAME_ROOT);

            processHeaderTemplateToRootDir(INSPECTOR_TEMPLATE_HEADER_NAME, templateData,
                    INSPECTOR_OUTPUT_FILE_NAME_ROOT);
            processSourceTemplateToRootDir(INSPECTOR_TEMPLATE_SOURCE_NAME, templateData,
                    INSPECTOR_OUTPUT_FILE_NAME_ROOT);
        }
    }

    private static final String IINSPECTOR_TEMPLATE_HEADER_NAME = "IInspectorParameterProvider.h.ftl";
    private static final String IINSPECTOR_OUTPUT_FILE_NAME_ROOT = "IInspectorParameterProvider";

    private static final String INSPECTOR_TEMPLATE_HEADER_NAME =
            "InspectorParameterProvider.h.ftl";
    private static final String INSPECTOR_TEMPLATE_SOURCE_NAME =
            "InspectorParameterProvider.cpp.ftl";
    private static final String INSPECTOR_OUTPUT_FILE_NAME_ROOT =
            "InspectorParameterProvider";

    private final List<SqlTableType> sqlTableTypes = new ArrayList<SqlTableType>();
}
