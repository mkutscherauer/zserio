import unittest
import zserio

from testutils import getZserioApi

class InstantiateViaImportTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "templates.zs").instantiate_via_import

    def testReadWrite(self):
        instantiateViaImport = self.api.InstantiateViaImport.fromFields(
            self.api.pkg.U32.fromFields(13),
            self.api.pkg.Test_string.fromFields("test")
        )

        writer = zserio.BitStreamWriter()
        instantiateViaImport.write(writer)
        reader = zserio.BitStreamReader(writer.getByteArray())
        readInstantiateViaImport = self.api.InstantiateViaImport()
        readInstantiateViaImport.read(reader)
        self.assertEqual(instantiateViaImport, readInstantiateViaImport)
