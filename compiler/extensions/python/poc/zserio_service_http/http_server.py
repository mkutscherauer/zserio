from flask import Flask, request

from SimpleServiceImpl import SimpleServiceImpl

app = Flask(__name__)

service = SimpleServiceImpl()

@app.route('/SimpleService/<procName>', methods=['POST'])
def simpleService(procName):
    requestData = request.get_data()
    print("procName:", procName, ", requestData: ", requestData)
    responseData = service.callProcedure("SimpleService." + procName, requestData)
    print("responseData: ", responseData)
    return responseData

if __name__ == '__main__':
    app.run()
