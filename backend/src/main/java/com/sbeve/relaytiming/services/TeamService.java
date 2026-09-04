package com.sbeve.relaytiming.services;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.TagEntity;
import com.sbeve.relaytiming.entities.TeamEntity;
import com.sbeve.relaytiming.entities.enums.Division;
import com.sbeve.relaytiming.entities.enums.RunnerStatus;
import com.sbeve.relaytiming.entities.enums.Sex;
import com.sbeve.relaytiming.entities.enums.TagType;
import com.sbeve.relaytiming.repositories.LapRecordRepository;
import com.sbeve.relaytiming.repositories.RunnerRepository;
import com.sbeve.relaytiming.repositories.TagRepository;
import com.sbeve.relaytiming.repositories.TeamRepository;
import com.sbeve.relaytiming.requests.CreateRunnerRequest;
import com.sbeve.relaytiming.requests.CreateTeamRequest;

@Service
public class TeamService {
    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    private final TeamRepository teamRepository;
    private final RunnerRepository runnerRepository;
    private final TagRepository tagRepository;
    private final LapRecordRepository lapRecordRepository;

    public TeamService(TeamRepository teamRepository, RunnerRepository runnerRepository,
            TagRepository tagRepository, LapRecordRepository lapRecordRepository) {
        this.teamRepository = teamRepository;
        this.runnerRepository = runnerRepository;
        this.tagRepository = tagRepository;
        this.lapRecordRepository = lapRecordRepository;
    }

    public TeamEntity createTeam(CreateTeamRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Team name is required");
        }
        if (request.division() == null || request.division().isBlank()) {
            throw new IllegalArgumentException("Division is required");
        }
        Division division = parseDivision(request.division());

        TagEntity tag = resolveTag(request.epcHex(), TagType.TEAM);
        if (teamRepository.existsByTag(tag)) {
            throw new IllegalStateException("Tag " + tag.getEpcHex() + " is already assigned to a team");
        }

        TeamEntity team = new TeamEntity(request.name(), tag);
        team.setDivision(division);
        return teamRepository.save(team);
    }

    public RunnerEntity createRunner(Long teamId, CreateRunnerRequest request) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team " + teamId + " not found"));

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Runner name is required");
        }
        if (request.leg() == null || request.leg() < 1) {
            throw new IllegalArgumentException("leg must be a positive integer");
        }
        if (runnerRepository.existsByTeamAndLeg(team, request.leg())) {
            throw new IllegalStateException("Team already has a runner at leg " + request.leg());
        }
        if (request.bib() == null || request.bib().isBlank()) {
            throw new IllegalArgumentException("bib is required");
        }
        if (request.sex() == null || request.sex().isBlank()) {
            throw new IllegalArgumentException("sex is required");
        }
        Sex sex = parseSex(request.sex());

        TagEntity tag = resolveTag(request.epcHex(), TagType.RUNNER);
        if (runnerRepository.existsByTag(tag)) {
            throw new IllegalStateException("Tag " + tag.getEpcHex() + " is already assigned to a runner");
        }

        RunnerStatus status = request.leg() == 1 ? RunnerStatus.ACTIVE : RunnerStatus.INACTIVE;
        RunnerEntity runner = new RunnerEntity(request.name(), status, request.leg(), tag, team);
        runner.setBib(request.bib());
        runner.setSex(sex);
        return runnerRepository.save(runner);
    }

    public TeamEntity getTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team " + teamId + " not found"));
    }

    public TeamEntity updateTeam(Long teamId, CreateTeamRequest request) {
        TeamEntity team = getTeam(teamId);

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Team name is required");
        }
        if (request.division() == null || request.division().isBlank()) {
            throw new IllegalArgumentException("Division is required");
        }
        Division division = parseDivision(request.division());

        TagEntity currentTag = team.getTag();
        if (!currentTag.getEpcHex().equals(request.epcHex())) {
            TagEntity newTag = resolveTag(request.epcHex(), TagType.TEAM);
            if (teamRepository.existsByTag(newTag)) {
                throw new IllegalStateException("Tag " + newTag.getEpcHex() + " is already assigned to a team");
            }
            team.setTag(newTag);
        }

        team.setName(request.name());
        team.setDivision(division);
        return teamRepository.save(team);
    }

    public void deleteTeam(Long teamId) {
        TeamEntity team = getTeam(teamId);
        List<RunnerEntity> runners = runnerRepository.findByTeamOrderByLeg(team);
        for (RunnerEntity runner : runners) {
            deleteRunnerCascade(runner);
        }

        TagEntity teamTag = team.getTag();
        teamRepository.delete(team);
        lapRecordRepository.deleteByTag(teamTag);
        tagRepository.delete(teamTag);
        log.atInfo().log("Deleted team {}", teamId);
    }

    public RunnerEntity updateRunner(Long teamId, Long runnerId, CreateRunnerRequest request) {
        TeamEntity team = getTeam(teamId);
        RunnerEntity runner = getRunnerForTeam(team, runnerId);

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Runner name is required");
        }
        if (request.leg() == null || request.leg() < 1) {
            throw new IllegalArgumentException("leg must be a positive integer");
        }
        if (!request.leg().equals(runner.getLeg()) && runnerRepository.existsByTeamAndLeg(team, request.leg())) {
            throw new IllegalStateException("Team already has a runner at leg " + request.leg());
        }
        if (request.bib() == null || request.bib().isBlank()) {
            throw new IllegalArgumentException("bib is required");
        }
        if (request.sex() == null || request.sex().isBlank()) {
            throw new IllegalArgumentException("sex is required");
        }
        Sex sex = parseSex(request.sex());

        TagEntity currentTag = runner.getTag();
        if (!currentTag.getEpcHex().equals(request.epcHex())) {
            TagEntity newTag = resolveTag(request.epcHex(), TagType.RUNNER);
            if (runnerRepository.existsByTag(newTag)) {
                throw new IllegalStateException("Tag " + newTag.getEpcHex() + " is already assigned to a runner");
            }
            runner.setTag(newTag);
        }

        runner.setName(request.name());
        runner.setLeg(request.leg());
        runner.setBib(request.bib());
        runner.setSex(sex);
        return runnerRepository.save(runner);
    }

    public RunnerEntity setRunnerStatus(Long teamId, Long runnerId, String status) {
        TeamEntity team = getTeam(teamId);
        RunnerEntity runner = getRunnerForTeam(team, runnerId);
        RunnerStatus newStatus = parseRunnerStatus(status);

        if (newStatus == RunnerStatus.ACTIVE) {
            List<RunnerEntity> teammates = runnerRepository.findByTeamOrderByLeg(team);
            for (RunnerEntity teammate : teammates) {
                if (!teammate.getId().equals(runner.getId()) && teammate.getStatus() == RunnerStatus.ACTIVE) {
                    teammate.setStatus(RunnerStatus.INACTIVE);
                    runnerRepository.save(teammate);
                }
            }
        }

        runner.setStatus(newStatus);
        return runnerRepository.save(runner);
    }

    public void deleteRunner(Long teamId, Long runnerId) {
        TeamEntity team = getTeam(teamId);
        RunnerEntity runner = getRunnerForTeam(team, runnerId);
        deleteRunnerCascade(runner);
        log.atInfo().log("Deleted runner {} from team {}", runnerId, teamId);
    }

    private RunnerEntity getRunnerForTeam(TeamEntity team, Long runnerId) {
        RunnerEntity runner = runnerRepository.findById(runnerId)
                .orElseThrow(() -> new NoSuchElementException("Runner " + runnerId + " not found"));
        if (!runner.getTeam().getId().equals(team.getId())) {
            throw new NoSuchElementException("Runner " + runnerId + " not found for team " + team.getId());
        }
        return runner;
    }

    private void deleteRunnerCascade(RunnerEntity runner) {
        TagEntity tag = runner.getTag();
        runnerRepository.delete(runner);
        lapRecordRepository.deleteByTag(tag);
        tagRepository.delete(tag);
    }

    public void wipeAll() {
        lapRecordRepository.deleteAll();
        runnerRepository.deleteAll();
        teamRepository.deleteAll();
        tagRepository.deleteAll();
        log.atInfo().log("Wiped all teams, runners, tags, and lap records");
    }

    private TagEntity resolveTag(String epcHex, TagType expectedType) {
        if (epcHex == null || epcHex.isBlank()) {
            throw new IllegalArgumentException("epcHex is required");
        }

        Optional<TagEntity> existing = tagRepository.findById(epcHex);
        if (existing.isPresent()) {
            if (existing.get().getTagType() != expectedType) {
                throw new IllegalStateException(
                        "Tag " + epcHex + " is already registered as " + existing.get().getTagType());
            }
            return existing.get();
        }

        return tagRepository.save(new TagEntity(epcHex, expectedType));
    }

    private Sex parseSex(String sex) {
        try {
            return Sex.valueOf(sex);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("sex must be M or F");
        }
    }

    private RunnerStatus parseRunnerStatus(String status) {
        try {
            return RunnerStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("status must be one of " + Arrays.toString(RunnerStatus.values()));
        }
    }

    private Division parseDivision(String division) {
        try {
            return Division.valueOf(division);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("division must be one of " + Arrays.toString(Division.values()));
        }
    }
}
