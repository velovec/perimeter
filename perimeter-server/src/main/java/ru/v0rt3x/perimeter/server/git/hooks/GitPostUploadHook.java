package ru.v0rt3x.perimeter.server.git.hooks;

import org.eclipse.jgit.storage.pack.PackStatistics;
import org.eclipse.jgit.transport.PostUploadHook;
import org.springframework.stereotype.Component;

@Component
public class GitPostUploadHook implements PostUploadHook {

    @Override
    public void onPostUpload(PackStatistics stats) {
        // Do nothing
    }
}
