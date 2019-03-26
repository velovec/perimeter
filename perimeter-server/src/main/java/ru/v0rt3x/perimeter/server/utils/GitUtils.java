package ru.v0rt3x.perimeter.server.utils;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import java.io.IOException;

public class GitUtils {

    public static RevObject getRevObject(ReceivePack rp, ReceiveCommand cmd) throws IOException {
        return rp.getRevWalk().parseAny(cmd.getNewId());
    }

    public static RevWalk getCommitLog(Repository repository) throws IOException {
        try (RevWalk revWalk = new RevWalk(repository)) {
            revWalk.sort(RevSort.COMMIT_TIME_DESC);
            for (Ref ref: repository.getRefDatabase().getRefs()) {
                revWalk.markStart(revWalk.parseCommit(ref.getLeaf().getObjectId()));
            }

            return revWalk;
        }
    }
}
