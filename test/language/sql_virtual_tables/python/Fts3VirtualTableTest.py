import unittest
import os

from testutils import getZserioApi, getApiDir

class Fts3VirtualTableTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "sql_virtual_tables.zs").fts3_virtual_table
        cls._fileName = os.path.join(getApiDir(os.path.dirname(__file__)), "fts3_virtual_table_test.sqlite")

    def setUp(self):
        if os.path.exists(self._fileName):
            os.remove(self._fileName)
        self._database = self.api.Fts3TestDb.fromFile(self._fileName)
        self._database.createSchema()

    def tearDown(self):
        self._database.close()

    def testDeleteTable(self):
        self.assertTrue(self._isTableInDb())

        testTable = self._database.getFts3VirtualTable()
        testTable.deleteTable()
        self.assertFalse(self._isTableInDb())

        testTable.createTable()
        self.assertTrue(self._isTableInDb())

    def testReadWithoutCondition(self):
        testTable = self._database.getFts3VirtualTable()

        writtenRows = self._createFts3VirtualTableRows()
        testTable.write(writtenRows)

        readRows = testTable.read()
        numReadRows = 0
        for readRow in readRows:
            self.assertEqual(writtenRows[numReadRows], readRow)
            numReadRows += 1
        self.assertTrue(len(writtenRows), numReadRows)

    def testReadWithCondition(self):
        testTable = self._database.getFts3VirtualTable()

        writtenRows = self._createFts3VirtualTableRows()
        testTable.write(writtenRows)

        condition = "body='Body1'"
        readRows = testTable.read(condition)

        expectedRowNum = 1
        for readRow in readRows:
            self.assertEqual(writtenRows[expectedRowNum], readRow)

    def testUpdate(self):
        testTable = self._database.getFts3VirtualTable()

        writtenRows = self._createFts3VirtualTableRows()
        testTable.write(writtenRows)

        updateTitle = "Title3"
        updateRow = self._createFts3VirtualTableRow(updateTitle, "UpdatedBody")
        updateCondition = "title='" + str(updateTitle) + "'"
        testTable.update(updateRow, updateCondition)

        readRows = testTable.read(updateCondition)
        for readRow in readRows:
            self.assertEqual(updateRow, readRow)

    def _createFts3VirtualTableRows(self):
        rows = []
        for rowId in range(self.NUM_VIRTUAL_TABLE_ROWS):
            rows.append(self._createFts3VirtualTableRow("Title" + str(rowId), "Body" + str(rowId)))

        return rows

    @staticmethod
    def _createFts3VirtualTableRow(title, body):
        return (title, body)

    def _isTableInDb(self):
        # check if database does contain table
        sqlQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + self.TABLE_NAME + "'"
        for row in self._database.connection().cursor().execute(sqlQuery):
            if len(row) == 1 and row[0] == self.TABLE_NAME:
                return True

        return False

    TABLE_NAME = "fts3VirtualTable"

    NUM_VIRTUAL_TABLE_ROWS = 5
