import unittest
import os

from testutils import getZserioApi, getApiDir

class SqlTableTemplatedFieldTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "templates.zs").sql_table_templated_field
        cls._fileName = os.path.join(getApiDir(os.path.dirname(__file__)), "sql_table_templated_field.sqlite")

    def setUp(self):
        if os.path.exists(self._fileName):
            os.remove(self._fileName)

    def testReadWrite(self):
        sqlTableTemplatedFieldDb = self.api.SqlTableTemplatedFieldDb.fromFile(self._fileName)
        sqlTableTemplatedFieldDb.createSchema()

        uint32Table = sqlTableTemplatedFieldDb.getUint32Table()
        uint32TableRows = [(0, self.api.Data_uint32.fromFields(42))]
        uint32Table.write(uint32TableRows)

        unionTable = sqlTableTemplatedFieldDb.getUnionTable()
        union1 = self.api.Union()
        union1.setValueString("string")
        unionTableRows = [(13, self.api.Data_Union.fromFields(union1))]
        unionTable.write(unionTableRows)

        sqlTableTemplatedFieldDb.close()

        readSqlTableTemplatedFieldDb = self.api.SqlTableTemplatedFieldDb.fromFile(self._fileName)
        readUint32TableIterator = readSqlTableTemplatedFieldDb.getUint32Table().read()
        readUint32TableRows = []
        for row in readUint32TableIterator:
            readUint32TableRows.append(row)
        readUnionTableIterator = readSqlTableTemplatedFieldDb.getUnionTable().read()
        readUnionTableRows = []
        for row in readUnionTableIterator:
            readUnionTableRows.append(row)

        self.assertEqual(uint32TableRows, readUint32TableRows)
        self.assertEqual(unionTableRows, readUnionTableRows)
