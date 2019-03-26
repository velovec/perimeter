package ru.v0rt3x.perimeter.server.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.git.dao.GitRepo;
import ru.v0rt3x.perimeter.server.git.dao.GitRepoRepository;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class GitRepositoryManager {

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private GitRepoRepository gitRepository;

    private static final Logger logger = LoggerFactory.getLogger(GitRepositoryManager.class);

    private Path resolveRepositoryPath(String repository) {
        return Paths.get(perimeterProperties.getGit().getRoot(), repository)
            .toAbsolutePath();
    }

    public boolean hasRepository(String name) {
        return Objects.nonNull(gitRepository.findByName(name));
    }

    public void createRepository(String name) throws GitAPIException, IOException {
        if (Objects.nonNull(gitRepository.findByName(name))) {
            throw new IllegalArgumentException("Repository already exists");
        }

        Path repositoryPath = resolveRepositoryPath(name);
        if (Files.exists(repositoryPath)) {
            Files.createDirectories(repositoryPath);
        }

        Repository repository = FileRepositoryBuilder.create(repositoryPath.toFile());
        repository.create(true);

        Path tmpRepoDir = Files.createTempDirectory(name);
        try (Git repo = Git.cloneRepository().setURI(repository.getDirectory().toURI().toString()).setDirectory(tmpRepoDir.toFile()).call()) {
            Path readme = tmpRepoDir.resolve("README.md");
            Files.write(readme, String.format("## %s", name).getBytes());

            repo.add().addFilepattern("README.md").call();

            repo.commit()
                .setAuthor("Perimeter Server", "perimeter@perimeter.io")
                .setMessage("Initial commit")
                .call();

            repo.push()
                .call();
        }
        deleteDirectoryStream(tmpRepoDir);

        GitRepo gitRepo = new GitRepo();

        gitRepo.setName(name);

        gitRepository.save(gitRepo);
    }

    public List<GitRepo> listRepositories() {
        return gitRepository.findAll();
    }

    public Repository getRepository(String repoName) throws IOException {
        if (repoName.startsWith("/")) {
            repoName = repoName.substring(1);
        }

        GitRepo gitRepo = gitRepository.findByName(repoName);

        if (Objects.nonNull(gitRepo)) {
            Path repositoryPath = resolveRepositoryPath(repoName);

            if (Files.exists(repositoryPath) && Files.isDirectory(repositoryPath)) {
                RepositoryCache.FileKey key = RepositoryCache.FileKey.lenient(repositoryPath.toFile(), FS.DETECTED);
                return key.open(true);
            } else {
                logger.warn("Unable to read Git repository: {}", repositoryPath);
            }
        }

        return null;
    }

    public void deleteRepository(String name) throws IOException {
        GitRepo gitRepo = gitRepository.findByName(name);
        if (Objects.isNull(gitRepo)) {
            throw new IllegalArgumentException("Repository not exists");
        }

        Path repoDirectory = resolveRepositoryPath(gitRepo.getName());
        deleteDirectoryStream(repoDirectory);
        gitRepository.delete(gitRepo);
    }

    private void deleteDirectoryStream(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    public GitRepo getGitRepository(String name) {
        return gitRepository.findByName(name);
    }
}
