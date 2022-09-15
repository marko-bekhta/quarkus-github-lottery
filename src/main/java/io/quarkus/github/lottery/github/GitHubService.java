package io.quarkus.github.lottery.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkiverse.githubapp.GitHubClientProvider;
import io.quarkiverse.githubapp.GitHubConfigFileProvider;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

@ApplicationScoped
public class GitHubService {

    @Inject
    GitHubClientProvider clientProvider;
    @Inject
    GitHubConfigFileProvider configFileProvider;

    public List<GitHubRepositoryRef> listRepositories() throws IOException {
        List<GitHubRepositoryRef> result = new ArrayList<>();
        GitHub client = clientProvider.getApplicationClient();
        for (GHAppInstallation installation : client.getApp().listInstallations()) {
            for (GHRepository repository : installation.listRepositories()) {
                result.add(new GitHubRepositoryRef(installation.getId(), repository.getFullName()));
            }
        }
        return result;
    }

    public GitHubRepository repository(GitHubRepositoryRef ref) {
        return new GitHubRepository(clientProvider, configFileProvider, ref);
    }
}
