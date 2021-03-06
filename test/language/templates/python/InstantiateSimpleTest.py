import unittest
import zserio

from testutils import getZserioApi

class InstantiateSimpleTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "templates.zs").instantiate_simple

    def testReadWrite(self):
        instantiateSimple = self.api.InstantiateSimple.fromFields(
            self.api.U32.fromFields(13)
        )

        writer = zserio.BitStreamWriter()
        instantiateSimple.write(writer)
        reader = zserio.BitStreamReader(writer.getByteArray())
        readInstantiateSimple = self.api.InstantiateSimple()
        readInstantiateSimple.read(reader)
        self.assertEqual(instantiateSimple, readInstantiateSimple)
