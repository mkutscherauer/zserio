#include <cstdio>
#include <string>
#include <fstream>

#include "gtest/gtest.h"

#include "sql_tables/TestDb.h"

namespace sql_tables
{
namespace blob_field_with_children_initialization_table
{

class BlobFieldWithChildrenInitializationTableTest : public ::testing::Test
{
public:
    BlobFieldWithChildrenInitializationTableTest()
    {
        std::remove(DB_FILE_NAME);

        m_database = new sql_tables::TestDb(DB_FILE_NAME);
        m_database->createSchema();
    }

    ~BlobFieldWithChildrenInitializationTableTest()
    {
        delete m_database;
    }

protected:
    static void fillRow(BlobFieldWithChildrenInitializationTableRow& row, size_t index)
    {
        row.setId(static_cast<uint32_t>(index));
        const uint32_t arrayLength = static_cast<uint32_t>(index);
        BlobWithChildrenInitialization blobWithChildrenInitialization;
        blobWithChildrenInitialization.setArrayLength(arrayLength);
        zserio::UInt32Array& array = blobWithChildrenInitialization.getParameterizedArray().getArray();
        for (uint32_t i = 0; i < arrayLength; ++i)
            array.push_back(i);
        row.setBlob(blobWithChildrenInitialization);
    }

    static void fillRows(std::vector<BlobFieldWithChildrenInitializationTableRow>& rows)
    {
        rows.clear();
        for (size_t i = 0; i < NUM_ROWS; ++i)
        {
            BlobFieldWithChildrenInitializationTableRow row;
            fillRow(row, i);
            rows.push_back(row);
        }
    }

    static void checkRow(const BlobFieldWithChildrenInitializationTableRow& row1,
            const BlobFieldWithChildrenInitializationTableRow& row2)
    {
        ASSERT_EQ(row1.getId(), row2.getId());
        ASSERT_EQ(row1.getBlob(), row2.getBlob());
    }

    static void checkRows(const std::vector<BlobFieldWithChildrenInitializationTableRow>& rows1,
            const std::vector<BlobFieldWithChildrenInitializationTableRow>& rows2)
    {
        ASSERT_EQ(rows1.size(), rows2.size());
        for (size_t i = 0; i < rows1.size(); ++i)
            checkRow(rows1[i], rows2[i]);
    }

    static const char DB_FILE_NAME[];
    static const size_t NUM_ROWS;

    sql_tables::TestDb* m_database;
};

const char BlobFieldWithChildrenInitializationTableTest::DB_FILE_NAME[] =
        "blob_field_with_children_initialization_table_test.sqlite";
const size_t BlobFieldWithChildrenInitializationTableTest::NUM_ROWS = 5;

TEST_F(BlobFieldWithChildrenInitializationTableTest, readWithoutCondition)
{
    BlobFieldWithChildrenInitializationTable& table = m_database->getBlobFieldWithChildrenInitializationTable();

    std::vector<BlobFieldWithChildrenInitializationTableRow> rows;
    fillRows(rows);
    table.write(rows);

    std::vector<BlobFieldWithChildrenInitializationTableRow> readRows;
    // we must use reserve to prevent dangling pointer to parameters in parameterizedBlob
    // once std::vector is reallocated!
    readRows.reserve(NUM_ROWS);
    table.read(readRows);

    checkRows(rows, readRows);
}

} // namespace blob_field_with_children_initialization_table
} // namespace sql_tables
