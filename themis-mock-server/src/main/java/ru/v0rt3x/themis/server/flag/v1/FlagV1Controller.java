package ru.v0rt3x.themis.server.flag.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.v0rt3x.themis.server.competition.CompetitionManager;
import ru.v0rt3x.themis.server.competition.CompetitionStage;
import ru.v0rt3x.themis.server.network.NetworkManager;
import ru.v0rt3x.themis.server.team.TeamManager;
import ru.v0rt3x.themis.server.utils.RandomUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

@RestController("/api/flag/v1")
public class FlagV1Controller {

    @Autowired
    private CompetitionManager competitionManager;

    @Autowired
    private TeamManager teamManager;

    @Autowired
    private NetworkManager networkManager;

    private static final Pattern FLAG_PATTERN = Pattern.compile(
        "^[\\d9a-f]{32}=$"
    );

    @RequestMapping(path = "/api/flag/v1/submit", method = RequestMethod.POST)
    public ResponseEntity<Integer> submitFlag(@RequestBody String flag, HttpServletRequest request) {
        if (!request.getHeader("Content-Type").equals("text/plain"))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SubmitResult.ERROR_FLAG_INVALID.ordinal());

//        String remoteAddress = request.getRemoteAddr();
//        if (!networkManager.isTeamNetwork(remoteAddress) || Objects.isNull(teamManager.getTeamByNetwork(remoteAddress)))
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SubmitResult.ERROR_ACCESS_DENIED.ordinal());

        if (Objects.isNull(flag) || !FLAG_PATTERN.matcher(flag).matches())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SubmitResult.ERROR_FLAG_INVALID.ordinal());

        if (!competitionManager.getStage().equals(CompetitionStage.STARTED) && !competitionManager.getStage().equals(CompetitionStage.STARTING))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SubmitResult.ERROR_COMPETITION_NOT_STARTED.ordinal());

        if (competitionManager.getStage().equals(CompetitionStage.PAUSED))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SubmitResult.ERROR_COMPETITION_PAUSED.ordinal());

        if (competitionManager.getStage().equals(CompetitionStage.FINISHED))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SubmitResult.ERROR_COMPETITION_FINISHED.ordinal());

        SubmitResult result = SubmitResult.SUCCESS;

        result = RandomUtils.setWithProbability(5.0, SubmitResult.ERROR_FLAG_YOUR_OWN, result);
        result = RandomUtils.setWithProbability(6.0, SubmitResult.ERROR_FLAG_SUBMITTED, result);
        result = RandomUtils.setWithProbability(7.0, SubmitResult.ERROR_FLAG_EXPIRED, result);
        result = RandomUtils.setWithProbability(8.0, SubmitResult.ERROR_RATELIMIT, result);

        return ResponseEntity.ok(result.ordinal());
    }

    @RequestMapping(path = "/api/flag/v1/info/{flag:[\\da-f]{32}=}", method = RequestMethod.GET)
    public ResponseEntity<FlagInfo> getFlagInfo(@PathVariable("flag") String flag) {
        if (RandomUtils.probabilityOf(35.0)) { // Return FLAG_NOT_FOUND in 35% requests
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Date nbf = new Date();
        Date exp = new Date(System.currentTimeMillis() + 300000L);

        return ResponseEntity.ok(
            new FlagInfo(flag, nbf, exp, competitionManager.getRound(), "<team name>", "<service name>")
        );
    }
}
