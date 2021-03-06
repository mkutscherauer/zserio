import unittest
import zserio

from testutils import getZserioApi

class ExpressionEnumTemplateArgumentTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "templates.zs").expression_enum_template_argument

    def testReadWrite(self):
        enumTemplateArgument_Color = self.api.EnumTemplateArgument_Color.fromFields(False, 10)
        self.assertTrue(enumTemplateArgument_Color.hasExpressionField())

        enumTemplateArgumentHolder = self.api.EnumTemplateArgumentHolder.fromFields(enumTemplateArgument_Color)
        writer = zserio.BitStreamWriter()
        enumTemplateArgumentHolder.write(writer)
        reader = zserio.BitStreamReader(writer.getByteArray())
        readEnumTemplateArgumentHolder = self.api.EnumTemplateArgumentHolder()
        readEnumTemplateArgumentHolder.read(reader)
        self.assertEqual(enumTemplateArgumentHolder, readEnumTemplateArgumentHolder)
