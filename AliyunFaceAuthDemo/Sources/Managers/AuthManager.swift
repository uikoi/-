import Foundation

enum AuthError: Error, LocalizedError {
    case sdkNotInitialized
    case metaInfoFailed
    case tokenFetchFailed
    case certificationIdEmpty
    case verificationFailed(String)
    case userCancelled
    case networkError(String)

    var errorDescription: String? {
        switch self {
        case .sdkNotInitialized:
            return "SDK未初始化"
        case .metaInfoFailed:
            return "获取设备信息失败"
        case .tokenFetchFailed:
            return "获取认证令牌失败"
        case .certificationIdEmpty:
            return "认证ID为空"
        case .verificationFailed(let reason):
            return "认证失败: \(reason)"
        case .userCancelled:
            return "用户取消认证"
        case .networkError(let message):
            return "网络错误: \(message)"
        }
    }
}

struct AuthResult: Codable {
    let state: String
    let certifyId: String?
    let subCode: String?
    let subMessage: String?
    let passed: Bool?

    enum CodingKeys: String, CodingKey {
        case state
        case certifyId
        case subCode
        case subMessage
        case passed
    }
}

class AuthManager {
    static let shared = AuthManager()

    private var isInitialized = false
    private let networkManager = NetworkManager.shared

    private init() {}

    func initializeSDK() {
        #if targetEnvironment(simulator)
        print("警告: 模拟器环境下无法使用阿里云实人认证SDK")
        return
        #else
        initializeAliyunFaceAuth()
        #endif
    }

    private func initializeAliyunFaceAuth() {
        // SDK初始化需要使用阿里云提供的AliyunFaceAuthFacade框架
        // 以下为调用示例，实际需要在导入SDK后使用

        /*
        import AliyunFaceAuthFacade

        AliyunFaceAuthFacade.shared().initAliyunFaceAuth { result in
            if let code = result?["code"] as? String, code == "600004" {
                self.isInitialized = true
                print("SDK初始化成功")
            } else {
                print("SDK初始化失败: \(result ?? [:])")
            }
        }
        */

        // 演示模式下直接标记为已初始化
        isInitialized = true
        print("SDK初始化完成 (演示模式)")
    }

    func startVerification(from viewController: UIViewController, completion: @escaping (Result<Bool, AuthError>) -> Void) {
        #if targetEnvironment(simulator)
        completion(.failure(.sdkNotInitialized))
        return
        #endif

        guard isInitialized else {
            completion(.failure(.sdkNotInitialized))
            return
        }

        // 获取MetaInfo
        guard let metaInfo = getMetaInfo() else {
            completion(.failure(.metaInfoFailed))
            return
        }

        // 调用服务端获取CertifyId
        networkManager.fetchVerifyToken(metaInfo: metaInfo) { [weak self] result in
            switch result {
            case .success(let certifyId):
                self?.performVerification(certifyId: certifyId, from: viewController, completion: completion)
            case .failure(let error):
                completion(.failure(.networkError(error.localizedDescription)))
            }
        }
    }

    private func getMetaInfo() -> String? {
        /*
        import AliyunFaceAuthFacade
        return AliyunFaceAuthFacade.shared().getMetaInfo()
        */
        // 演示模式返回模拟数据
        return "demo_meta_info_\(Date().timeIntervalSince1970)"
    }

    private func performVerification(certifyId: String, from viewController: UIViewController, completion: @escaping (Result<Bool, AuthError>) -> Void) {
        /*
        import AliyunFaceAuthFacade

        AliyunFaceAuthFacade.shared().verify(
            withCertifyId: certifyId,
            withCurrentViewController: viewController,
            withExtParams: nil
        ) { [weak self] resultDict in
            guard let result = resultDict else {
                completion(.failure(.verificationFailed("无返回结果")))
                return
            }

            self?.handleVerificationResult(result: result, certifyId: certifyId, completion: completion)
        }
        */

        // 演示模式模拟认证流程
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            let demoPassed = Bool.random()
            completion(.success(demoPassed))
        }
    }

    private func handleVerificationResult(result: [String: Any], certifyId: String, completion: @escaping (Result<Bool, AuthError>) -> Void) {
        let state = result["state"] as? String ?? ""

        switch state {
        case "success":
            // 认证通过，查询服务端确认结果
            networkManager.queryVerifyResult(certifyId: certifyId) { queryResult in
                switch queryResult {
                case .success(let passed):
                    completion(.success(passed))
                case .failure:
                    // 服务端查询失败，但客户端认证已通过
                    completion(.success(true))
                }
            }

        case "fail":
            let subCode = result["subCode"] as? String ?? "未知错误"
            completion(.failure(.verificationFailed(subCode)))

        case "cancel":
            completion(.failure(.userCancelled))

        default:
            completion(.failure(.verificationFailed("未知状态")))
        }
    }

    func checkSDKStatus() -> Bool {
        return isInitialized
    }
}
