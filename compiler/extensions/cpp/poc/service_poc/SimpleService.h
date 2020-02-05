/**
 * Automatically generated by Zserio C++ extension version 1.4.0-pre2.
 */

#ifndef SIMPLE_SERVICE_H
#define SIMPLE_SERVICE_H

#include <array>
#include <map>
#include <string>
#include <vector>
#include <functional>

#include <zserio_runtime/IService.h>
#include <zserio_runtime/ServiceException.h>

#include "service_poc/Request.h"
#include "service_poc/Response.h"

namespace service_poc
{

namespace SimpleService
{
    class Service : public ::zserio::IService
    {
    public:
        Service();
        virtual ~Service() = default;

        Service(const Service&) = default;
        Service& operator=(const Service&) = default;

        Service(Service&&) = default;
        Service& operator=(Service&&) = default;

        void callMethod(const std::string& methodName, const std::vector<uint8_t>& requestData,
                std::vector<uint8_t>& responseData, void* context = nullptr) override;

        static const char* serviceFullName() noexcept;
        static const ::std::array<const char*, 2>& methodNames() noexcept;

    private:
        virtual void powerOfTwoImpl(const Request& request, Response& response) = 0;
        virtual void powerOfFourImpl(const Request& request, Response& response) = 0;

        void powerOfTwoMethod(const std::vector<uint8_t>& requestData, std::vector<uint8_t>& responseData);
        void powerOfFourMethod(const std::vector<uint8_t>& requestData, std::vector<uint8_t>& responseData);

        using Method = std::function<void(const std::vector<uint8_t>& requestData,
                std::vector<uint8_t>& responseData)>;

        std::map<std::string, Method> m_methodMap;
    };

    class Client
    {
    public:
        explicit Client(::zserio::IService& service);
        ~Client() = default;

        Client(const Client&) = default;
        Client& operator=(const Client&) = default;

        Client(Client&&) = default;
        Client& operator=(Client&&) = default;

        void powerOfTwoMethod(Request& request, Response& response);
        void powerOfFourMethod(Request& request, Response& response);

    private:
        zserio::IService& m_service;
    };
} // namespace SimpleService

} // namespace service_poc

#endif // ifndef SIMPLE_SERVICE_H
