#include <cstdio>
#include <vector>
#include <string>

#include "gtest/gtest.h"

#include "zserio/StringConvertUtil.h"

#include "explicit_parameters/ExplicitParametersDb.h"

namespace explicit_parameters
{
namespace explicit_enum_param
{

class ExplicitEnumParamTest : public ::testing::Test
{
public:
    ExplicitEnumParamTest()
    {
        std::remove(DB_FILE_NAME);

        m_database = new ExplicitParametersDb(DB_FILE_NAME);
        m_database->createSchema();
    }

    ~ExplicitEnumParamTest()
    {
        delete m_database;
    }

protected:
    void fillEnumParamTableRow(EnumParamTable::Row& row, uint32_t id, const std::string& name)
    {
        row.setId(id);
        row.setName(name);

        TestBlob testBlob1;
        std::vector<uint8_t>& values1 = testBlob1.getValues();
        for (uint32_t i = 0; i < ::zserio::enumToValue(ENUM_PARAM_TABLE_COUNT1); ++i)
            values1.push_back(static_cast<uint8_t>(id));
        row.setBlob1(testBlob1);

        TestBlob testBlob2;
        std::vector<uint8_t>& values2 = testBlob2.getValues();
        for (uint32_t i = 0; i < ::zserio::enumToValue(ENUM_PARAM_TABLE_COUNT2); ++i)
            values2.push_back(static_cast<uint8_t>(id + 1));
        row.setBlob2(testBlob2);

        TestBlob testBlob3;
        std::vector<uint8_t>& values3 = testBlob3.getValues();
        for (uint32_t i = 0; i < ::zserio::enumToValue(ENUM_PARAM_TABLE_COUNT1); ++i)
            values3.push_back(static_cast<uint8_t>(id + 2));
        row.setBlob3(testBlob3);
    }

    void fillEnumParamTableRows(std::vector<EnumParamTable::Row>& rows)
    {
        rows.clear();
        for (uint32_t id = 0; id < NUM_ENUM_PARAM_TABLE_ROWS; ++id)
        {
            const std::string name = "Name" + zserio::convertToString(id);
            EnumParamTable::Row row;
            fillEnumParamTableRow(row, id, name);
            rows.push_back(row);
        }
    }

    static void checkEnumParamTableRow(const EnumParamTable::Row& row1, const EnumParamTable::Row& row2)
    {
        ASSERT_EQ(row1.getId(), row2.getId());
        ASSERT_EQ(row1.getName(), row2.getName());
        ASSERT_EQ(row1.getBlob1(), row2.getBlob1());
        ASSERT_EQ(row1.getBlob2(), row2.getBlob2());
        ASSERT_EQ(row1.getBlob3(), row2.getBlob3());

        // check reused explicit count parameter
        ASSERT_EQ(row2.getBlob1().getCount(), row2.getBlob3().getCount());
    }

    static void checkEnumParamTableRows(const std::vector<EnumParamTable::Row>& rows1,
            const std::vector<EnumParamTable::Row>& rows2)
    {
        ASSERT_EQ(rows1.size(), rows2.size());
        for (size_t i = 0; i < rows1.size(); ++i)
            checkEnumParamTableRow(rows1[i], rows2[i]);
    }

    class EnumParamTableParameterProvider : public EnumParamTable::IParameterProvider
    {
    public:
        virtual TestEnum getCount1(EnumParamTable::Row&)
        {
            return ENUM_PARAM_TABLE_COUNT1;
        }

        virtual TestEnum getCount2(EnumParamTable::Row&)
        {
            return ENUM_PARAM_TABLE_COUNT2;
        }
    };

    ExplicitParametersDb* m_database;

    static const char DB_FILE_NAME[];

    static const uint32_t NUM_ENUM_PARAM_TABLE_ROWS;
    static const TestEnum ENUM_PARAM_TABLE_COUNT1;
    static const TestEnum ENUM_PARAM_TABLE_COUNT2;
};

const char ExplicitEnumParamTest::DB_FILE_NAME[] = "explicit_enum_param_test.sqlite";

const uint32_t ExplicitEnumParamTest::NUM_ENUM_PARAM_TABLE_ROWS = 5;
const TestEnum ExplicitEnumParamTest::ENUM_PARAM_TABLE_COUNT1 = TestEnum::TEN;
const TestEnum ExplicitEnumParamTest::ENUM_PARAM_TABLE_COUNT2 = TestEnum::ELEVEN;

TEST_F(ExplicitEnumParamTest, readWithoutCondition)
{
    EnumParamTable& enumParamTable = m_database->getEnumParamTable();

    EnumParamTableParameterProvider parameterProvider;
    std::vector<EnumParamTable::Row> writtenRows;
    fillEnumParamTableRows(writtenRows);
    enumParamTable.write(parameterProvider, writtenRows);

    EnumParamTable::Reader reader = enumParamTable.createReader(parameterProvider);

    std::vector<EnumParamTable::Row> readRows;
    while (reader.hasNext())
        readRows.push_back(reader.next());
    checkEnumParamTableRows(writtenRows, readRows);
}

TEST_F(ExplicitEnumParamTest, readWithCondition)
{
    EnumParamTable& enumParamTable = m_database->getEnumParamTable();

    EnumParamTableParameterProvider parameterProvider;
    std::vector<EnumParamTable::Row> writtenRows;
    fillEnumParamTableRows(writtenRows);
    enumParamTable.write(parameterProvider, writtenRows);

    const std::string condition = "name='Name1'";
    EnumParamTable::Reader reader = enumParamTable.createReader(parameterProvider, condition);

    ASSERT_TRUE(reader.hasNext());
    EnumParamTable::Row readRow = reader.next();
    ASSERT_FALSE(reader.hasNext());

    const size_t expectedRowNum = 1;
    checkEnumParamTableRow(writtenRows[expectedRowNum], readRow);
}

TEST_F(ExplicitEnumParamTest, update)
{
    EnumParamTable& enumParamTable = m_database->getEnumParamTable();

    EnumParamTableParameterProvider parameterProvider;
    std::vector<EnumParamTable::Row> writtenRows;
    fillEnumParamTableRows(writtenRows);
    enumParamTable.write(parameterProvider, writtenRows);

    const uint64_t updateRowId = 3;
    EnumParamTable::Row updateRow;
    fillEnumParamTableRow(updateRow, updateRowId, "UpdatedName");
    const std::string updateCondition = "id=" + zserio::convertToString(updateRowId);
    enumParamTable.update(parameterProvider, updateRow, updateCondition);

    EnumParamTable::Reader reader = enumParamTable.createReader(parameterProvider, updateCondition);

    ASSERT_TRUE(reader.hasNext());
    EnumParamTable::Row readRow = reader.next();
    ASSERT_FALSE(reader.hasNext());
    checkEnumParamTableRow(updateRow, readRow);
}

} // namespace explicit_enum_param
} // namespace explicit_parameters
