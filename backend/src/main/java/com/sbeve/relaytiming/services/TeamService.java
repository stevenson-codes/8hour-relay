package com.sbeve.relaytiming.services;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.TagEntity;
import com.sbeve.relaytiming.entities.TeamEntity;
import com.sbeve.relaytiming.entities.enums.RunnerStatus;
import com.sbeve.relaytiming.entities.enums.Sex;
import com.sbeve.relaytiming.entities.enums.TagType;
import com.sbeve.relaytiming.repositories.RunnerRepository;
import com.sbeve.relaytiming.repositories.TagRepository;
import com.sbeve.relaytiming.repositories.TeamRepository;
import com.sbeve.relaytiming.requests.CreateRunnerRequest;
import com.sbeve.relaytiming.requests.CreateTeamRequest;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final RunnerRepository runnerRepository;
    private final TagRepository tagRepository;

    public TeamService(TeamRepository teamRepository, RunnerRepository runnerRepository,
            TagRepository tagRepository) {
        this.teamRepository = teamRepository;
        this.runnerRepository = runnerRepository;
        this.tagRepository = tagRepository;
    }

    public TeamEntity createTeam(CreateTeamRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Team name is required");
        }

        TagEntity tag = resolveTag(request.epcHex(), TagType.TEAM);
        if (teamRepository.existsByTag(tag)) {
            throw new IllegalStateException("Tag " + tag.getEpcHex() + " is already assigned to a team");
        }

        TeamEntity team = new TeamEntity(request.name(), tag);
        team.setDivision(blankToNull(request.division()));
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

        Sex sex = parseSex(request.sex());

        TagEntity tag = resolveTag(request.epcHex(), TagType.RUNNER);
        if (runnerRepository.existsByTag(tag)) {
            throw new IllegalStateException("Tag " + tag.getEpcHex() + " is already assigned to a runner");
        }

        RunnerStatus status = request.leg() == 1 ? RunnerStatus.ACTIVE : RunnerStatus.INACTIVE;
        RunnerEntity runner = new RunnerEntity(request.name(), status, request.leg(), tag, team);
        runner.setBib(blankToNull(request.bib()));
        runner.setSex(sex);
        return runnerRepository.save(runner);
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
        if (sex == null || sex.isBlank()) {
            return null;
        }
        try {
            return Sex.valueOf(sex);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("sex must be M or F");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
