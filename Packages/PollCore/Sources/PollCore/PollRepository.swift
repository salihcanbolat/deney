import Foundation

/// Ekranlar veriyi yalnızca bu protokol üzerinden alır. Örnek veriden canlı backend'e
/// geçerken (Aşama 6) yalnızca uygulayıcı değişir.
public protocol PollRepository: Sendable {
    func parties() async throws -> [Party]
    func polls() async throws -> [Poll]
    func result(for pollId: Poll.ID) async throws -> PollResult
}

public enum PollRepositoryError: Error, Equatable {
    case unknownPoll(Poll.ID)
}

public struct MockPollRepository: PollRepository {
    public let dataset: MockDataset

    public init(dataset: MockDataset = .standard) {
        self.dataset = dataset
    }

    public func parties() async throws -> [Party] { dataset.parties }

    public func polls() async throws -> [Poll] { dataset.polls }

    public func result(for pollId: Poll.ID) async throws -> PollResult {
        guard let result = dataset.result(for: pollId) else { throw PollRepositoryError.unknownPoll(pollId) }
        return result
    }
}
