import unittest
import zserio

from testutils import getZserioApi

class StructTemplatedTypeArgumentTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "templates.zs").struct_templated_type_argument

    def testReadWrite(self):
        paramHolder = self.api.ParamHolder_uint32.fromFields(42)
        structTemplatedTypeArgument = self.api.StructTemplatedTypeArgument.fromFields(
            paramHolder,
            self.api.Parameterized_uint32.fromFields(paramHolder, "description", 13)
        )

        writer = zserio.BitStreamWriter()
        structTemplatedTypeArgument.write(writer)
        reader = zserio.BitStreamReader(writer.getByteArray())
        readStructTemplatedTypeArgument = self.api.StructTemplatedTypeArgument()
        readStructTemplatedTypeArgument.read(reader)
        self.assertEqual(structTemplatedTypeArgument, readStructTemplatedTypeArgument)
