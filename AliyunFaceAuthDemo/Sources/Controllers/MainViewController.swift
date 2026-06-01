import UIKit
import SnapKit

class MainViewController: UIViewController {

    private let authManager = AuthManager.shared
    private let activityIndicator = UIActivityIndicatorView(style: .large)

    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "阿里云金融级实人认证"
        label.font = .systemFont(ofSize: 24, weight: .bold)
        label.textAlignment = .center
        label.textColor = .black
        return label
    }()

    private lazy var subtitleLabel: UILabel = {
        let label = UILabel()
        label.text = "完成身份认证，确保账户安全"
        label.font = .systemFont(ofSize: 14)
        label.textAlignment = .center
        label.textColor = .gray
        return label
    }()

    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.tintColor = .systemBlue
        if #available(iOS 13.0, *) {
            imageView.image = UIImage(systemName: "faceid")
        }
        return imageView
    }()

    private lazy var authButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("开始人脸认证", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 18, weight: .semibold)
        button.backgroundColor = .systemBlue
        button.setTitleColor(.white, for: .normal)
        button.layer.cornerRadius = 12
        button.addTarget(self, action: #selector(authButtonTapped), for: .touchUpInside)
        return button
    }()

    private lazy var statusLabel: UILabel = {
        let label = UILabel()
        label.text = "SDK状态: 未初始化"
        label.font = .monospacedSystemFont(ofSize: 12, weight: .regular)
        label.textAlignment = .center
        label.textColor = .orange
        label.numberOfLines = 0
        return label
    }()

    private lazy var instructionLabel: UILabel = {
        let label = UILabel()
        label.text = "认证说明:\n1. 请确保在光线充足的环境下进行\n2. 认证时需要直视摄像头\n3. 部分认证方案需要配合动作完成"
        label.font = .systemFont(ofSize: 13)
        label.textColor = .darkGray
        label.numberOfLines = 0
        label.textAlignment = .left
        return label
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        checkSDKStatus()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        checkSDKStatus()
    }

    private func setupUI() {
        view.backgroundColor = .white
        title = "身份认证"

        view.addSubview(titleLabel)
        view.addSubview(subtitleLabel)
        view.addSubview(iconImageView)
        view.addSubview(authButton)
        view.addSubview(statusLabel)
        view.addSubview(instructionLabel)
        view.addSubview(activityIndicator)

        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(view.safeAreaLayoutGuide).offset(40)
            make.leading.trailing.equalToSuperview().inset(20)
        }

        subtitleLabel.snp.makeConstraints { make in
            make.top.equalTo(titleLabel.snp.bottom).offset(8)
            make.leading.trailing.equalToSuperview().inset(20)
        }

        iconImageView.snp.makeConstraints { make in
            make.top.equalTo(subtitleLabel.snp.bottom).offset(50)
            make.centerX.equalToSuperview()
            make.width.height.equalTo(120)
        }

        authButton.snp.makeConstraints { make in
            make.top.equalTo(iconImageView.snp.bottom).offset(60)
            make.leading.trailing.equalToSuperview().inset(40)
            make.height.equalTo(56)
        }

        statusLabel.snp.makeConstraints { make in
            make.top.equalTo(authButton.snp.bottom).offset(20)
            make.leading.trailing.equalToSuperview().inset(20)
        }

        instructionLabel.snp.makeConstraints { make in
            make.top.equalTo(statusLabel.snp.bottom).offset(30)
            make.leading.trailing.equalToSuperview().inset(30)
        }

        activityIndicator.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
    }

    private func checkSDKStatus() {
        let isReady = authManager.checkSDKStatus()

        if isReady {
            #if targetEnvironment(simulator)
            statusLabel.text = "SDK状态: 演示模式 (模拟器不支持)\n认证结果将随机模拟"
            statusLabel.textColor = .orange
            #else
            statusLabel.text = "SDK状态: 已就绪 ✓"
            statusLabel.textColor = .systemGreen
            #endif
        } else {
            statusLabel.text = "SDK状态: 未初始化\n请检查SDK配置"
            statusLabel.textColor = .red
        }
    }

    @objc private func authButtonTapped() {
        #if targetEnvironment(simulator)
        showSimulatorAlert()
        return
        #endif

        setUIEnabled(false)
        startActivityIndicator()

        authManager.startVerification(from: self) { [weak self] result in
            DispatchQueue.main.async {
                self?.stopActivityIndicator()
                self?.setUIEnabled(true)
                self?.handleAuthResult(result)
            }
        }
    }

    private func handleAuthResult(_ result: Result<Bool, AuthError>) {
        switch result {
        case .success(let passed):
            showResultAlert(passed: passed)

        case .failure(let error):
            showErrorAlert(error: error)
        }
    }

    private func showResultAlert(passed: Bool) {
        let title = passed ? "认证通过" : "认证失败"
        let message = passed ? "恭喜您已完成身份认证，身份核验成功！" : "认证未通过，请稍后重试或联系客服"

        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)

        if passed {
            alert.addAction(UIAlertAction(title: "完成", style: .default))
        } else {
            alert.addAction(UIAlertAction(title: "重试", style: .default) { [weak self] _ in
                self?.authButtonTapped()
            })
            alert.addAction(UIAlertAction(title: "取消", style: .cancel))
        }

        present(alert, animated: true)
    }

    private func showErrorAlert(error: AuthError) {
        var message = error.localizedDescription ?? "未知错误"

        switch error {
        case .userCancelled:
            message = "您已取消认证"
        case .sdkNotInitialized:
            message = "SDK未正确初始化，请检查配置"
        case .metaInfoFailed:
            message = "获取设备信息失败，请检查网络"
        case .tokenFetchFailed:
            message = "获取认证令牌失败，请稍后重试"
        case .networkError(let detail):
            message = "网络错误: \(detail)"
        default:
            break
        }

        let alert = UIAlertController(title: "认证出错", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }

    private func showSimulatorAlert() {
        let alert = UIAlertController(
            title: "模拟器提示",
            message: "真机测试模式下SDK才能正常工作。\n当前为演示模式，认证结果将随机模拟。",
            preferredStyle: .alert
        )

        alert.addAction(UIAlertAction(title: "继续演示", style: .default) { [weak self] _ in
            self?.simulateAuthFlow()
        })

        alert.addAction(UIAlertAction(title: "取消", style: .cancel))

        present(alert, animated: true)
    }

    private func simulateAuthFlow() {
        setUIEnabled(false)
        startActivityIndicator()
        statusLabel.text = "认证状态: 认证中..."

        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { [weak self] in
            self?.stopActivityIndicator()
            self?.setUIEnabled(true)
            self?.statusLabel.text = "SDK状态: 已就绪 ✓"

            let passed = Bool.random()
            self?.showResultAlert(passed: passed)
        }
    }

    private func setUIEnabled(_ enabled: Bool) {
        authButton.isEnabled = enabled
        authButton.alpha = enabled ? 1.0 : 0.6
    }

    private func startActivityIndicator() {
        activityIndicator.startAnimating()
    }

    private func stopActivityIndicator() {
        activityIndicator.stopAnimating()
    }
}
