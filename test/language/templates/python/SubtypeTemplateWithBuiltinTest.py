import unittest
import zserio

from testutils import getZserioApi

class SubtypeTemplateWithBuiltinTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "templates.zs").subtype_template_with_builtin

    def testReadWrite(self):
        subtypeTemplateWithBuiltin = self.api.SubtypeTemplateWithBuiltin.fromFields(
            self.api.TestStructure_uint32.fromFields(13)
        )

        writer = zserio.BitStreamWriter()
        subtypeTemplateWithBuiltin.write(writer)
        reader = zserio.BitStreamReader(writer.getByteArray())
        readSubtypeTemplateWithBuiltin = self.api.SubtypeTemplateWithBuiltin()
        readSubtypeTemplateWithBuiltin.read(reader)
        self.assertEqual(subtypeTemplateWithBuiltin, readSubtypeTemplateWithBuiltin)
