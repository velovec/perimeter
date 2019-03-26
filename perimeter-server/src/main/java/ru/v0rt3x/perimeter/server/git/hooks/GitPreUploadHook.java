package ru.v0rt3x.perimeter.server.git.hooks;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.PreUploadHook;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.UploadPack;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class GitPreUploadHook implements PreUploadHook {

    @Override
    public void onBeginNegotiateRound(UploadPack up, Collection<? extends ObjectId> wants, int cntOffered) throws ServiceMayNotContinueException {
        // Do nothing
    }

    @Override
    public void onEndNegotiateRound(UploadPack up, Collection<? extends ObjectId> wants, int cntCommon, int cntNotFound, boolean ready) throws ServiceMayNotContinueException {
        // Do nothing
    }

    @Override
    public void onSendPack(UploadPack up, Collection<? extends ObjectId> wants, Collection<? extends ObjectId> haves) throws ServiceMayNotContinueException {
        // Do nothing
    }
}
