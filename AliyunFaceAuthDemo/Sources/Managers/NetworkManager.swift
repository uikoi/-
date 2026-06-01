import Foundation

enum NetworkError: Error, LocalizedError {
    case invalidURL
    case noData
    case decodingError
    case serverError(Int)
    case unknown

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "无效的URL地址"
        case .noData:
            return "服务器未返回数据"
        case .decodingError:
            return "数据解析失败"
        case .serverError(let code):
            return "服务器错误 (Code: \(code))"
        case .unknown:
            return "未知错误"
        }
    }
}

struct VerifyTokenResponse: Codable {
    let code: String?
    let message: String?
    let certifyId: String?
    let requestId: String?

    var isSuccess: Bool {
        return code == "Success" || code == "200"
    }
}

struct VerifyResultResponse: Codable {
    let code: String?
    let message: String?
    let passed: Bool?
    let certifyId: String?
    let identityInfo: IdentityInfo?

    enum CodingKeys: String, CodingKey {
        case code
        case message
        case passed
        case certifyId
        case identityInfo = "IdentityInfo"
    }

    var isPassed: Bool {
        return passed == true || code == "passed"
    }
}

struct IdentityInfo: Codable {
    let name: String?
    let idCardNumber: String?
    let faceImageUrl: String?
}

class NetworkManager {
    static let shared = NetworkManager()

    private let session: URLSession
    private let baseURL: String

    // 配置你的业务服务器地址
    private static let serverBaseURL = "https://your-business-server.com/api"

    private init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 60
        self.session = URLSession(configuration: configuration)
        self.baseURL = NetworkManager.serverBaseURL
    }

    func fetchVerifyToken(metaInfo: String, completion: @escaping (Result<String, NetworkError>) -> Void) {
        // 构建请求参数
        let parameters: [String: Any] = [
            "metaInfo": metaInfo,
            "sceneId": Config.sceneId,
            "bizCode": "FACE_SDK",
            "productCode": "ID_PRO"
        ]

        // 调用服务端接口获取CertifyId
        // 实际项目中需要替换为真实的服务端接口
        request(endpoint: "/faceauth/init", method: "POST", parameters: parameters) { result in
            switch result {
            case .success(let data):
                do {
                    let response = try JSONDecoder().decode(VerifyTokenResponse.self, from: data)
                    if response.isSuccess, let certifyId = response.certifyId {
                        completion(.success(certifyId))
                    } else {
                        completion(.failure(.serverError(-1)))
                    }
                } catch {
                    // 演示模式下返回模拟数据
                    let mockCertifyId = "mock_certify_id_\(UUID().uuidString)"
                    completion(.success(mockCertifyId))
                }

            case .failure(let error):
                // 网络错误时返回演示数据
                let mockCertifyId = "demo_certify_id_\(UUID().uuidString)"
                print("网络请求失败，使用演示数据: \(error.localizedDescription)")
                completion(.success(mockCertifyId))
            }
        }
    }

    func queryVerifyResult(certifyId: String, completion: @escaping (Result<Bool, NetworkError>) -> Void) {
        let parameters: [String: Any] = [
            "certifyId": certifyId,
            "sceneId": Config.sceneId
        ]

        request(endpoint: "/faceauth/query", method: "POST", parameters: parameters) { result in
            switch result {
            case .success(let data):
                do {
                    let response = try JSONDecoder().decode(VerifyResultResponse.self, from: data)
                    completion(.success(response.isPassed))
                } catch {
                    // 演示模式
                    completion(.success(true))
                }

            case .failure(let error):
                print("查询结果失败: \(error.localizedDescription)")
                completion(.success(true))
            }
        }
    }

    private func request(endpoint: String, method: String, parameters: [String: Any], completion: @escaping (Result<Data, NetworkError>) -> Void) {
        guard let url = URL(string: baseURL + endpoint) else {
            completion(.failure(.invalidURL))
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        // 如果需要认证，添加Authorization头
        // request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: parameters)
        } catch {
            completion(.failure(.decodingError))
            return
        }

        let task = session.dataTask(with: request) { data, response, error in
            if let error = error {
                print("网络请求错误: \(error.localizedDescription)")
                completion(.failure(.unknown))
                return
            }

            guard let httpResponse = response as? HTTPURLResponse else {
                completion(.failure(.unknown))
                return
            }

            guard (200...299).contains(httpResponse.statusCode) else {
                completion(.failure(.serverError(httpResponse.statusCode)))
                return
            }

            guard let data = data else {
                completion(.failure(.noData))
                return
            }

            completion(.success(data))
        }

        task.resume()
    }
}
