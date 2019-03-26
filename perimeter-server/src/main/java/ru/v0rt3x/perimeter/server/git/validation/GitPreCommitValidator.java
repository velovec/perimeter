package ru.v0rt3x.perimeter.server.git.validation;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.ReceiveCommand;

@FunctionalInterface
public interface GitPreCommitValidator {

    void validate(ReceiveCommand cmd, RevCommit revCommit);
}
