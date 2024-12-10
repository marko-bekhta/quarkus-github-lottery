package io.quarkus.github.lottery.history;

import static io.quarkus.github.lottery.util.UncheckedIOFunction.uncheckedIO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.github.lottery.config.LotteryConfig;
import io.quarkus.github.lottery.draw.DrawRef;
import io.quarkus.github.lottery.draw.LotteryReport;
import io.quarkus.github.lottery.github.GitHubRepository;
import io.quarkus.github.lottery.github.GitHubService;
import io.quarkus.github.lottery.github.TopicRef;
import io.quarkus.github.lottery.message.MessageFormatter;
import io.quarkus.github.lottery.notification.NotificationService;

@ApplicationScoped
public class HistoryService {

    @Inject
    GitHubService gitHubService;
    @Inject
    MessageFormatter messageFormatter;

    public LotteryHistory fetch(DrawRef drawRef, LotteryConfig config) throws IOException {
        var persistenceRepo = persistenceRepo(drawRef, config);
        var history = new LotteryHistory(drawRef.instant(), config.buckets());
        persistenceRepo.topic(historyTopic(drawRef)).extractComments(history.since())
                .flatMap(uncheckedIO(message -> messageFormatter.extractPayloadFromHistoryBodyMarkdown(message).stream()))
                .forEach(history::add);
        return history;
    }

    public void append(DrawRef drawRef, LotteryConfig config, List<LotteryReport.Serialized> reports) throws IOException {
        var persistenceRepo = persistenceRepo(drawRef, config);
        String body = messageFormatter.formatHistoryBodyMarkdown(drawRef, reports);
        persistenceRepo.topic(historyTopic(drawRef)).update("", body, true);
    }

    private TopicRef historyTopic(DrawRef drawRef) {
        return TopicRef.history(messageFormatter.formatHistoryTopicText(drawRef));
    }

    GitHubRepository persistenceRepo(DrawRef drawRef, LotteryConfig config) {
        // We persist history to the same repository we send notifications to
        return NotificationService.notificationRepository(gitHubService, drawRef, config.notifications());
    }
}
