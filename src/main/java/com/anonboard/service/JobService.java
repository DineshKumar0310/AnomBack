package com.anonboard.service;

import com.anonboard.model.Job;
import com.anonboard.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public List<Job> getActiveJobs(Job.JobType type) {
        if (type != null) {
            return jobRepository.findByActiveTrueAndTypeOrderByPostedDateDesc(type);
        }
        return jobRepository.findByActiveTrueOrderByPostedDateDesc();
    }

    public List<Job> getAllJobsForAdmin() {
        return jobRepository.findAll();
    }

    public Job getJobById(String id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }

    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    public Job updateJob(String id, Job jobDetails) {
        Job job = getJobById(id);

        job.setCompanyName(jobDetails.getCompanyName());
        job.setLogoUrl(jobDetails.getLogoUrl());
        job.setTitle(jobDetails.getTitle());
        job.setType(jobDetails.getType());
        job.setDuration(jobDetails.getDuration());
        job.setLocation(jobDetails.getLocation());
        job.setEligibility(jobDetails.getEligibility());
        job.setEligibleDegrees(jobDetails.getEligibleDegrees());
        job.setEligibleBranches(jobDetails.getEligibleBranches());
        job.setEligibleBatches(jobDetails.getEligibleBatches());
        job.setExperienceLevel(jobDetails.getExperienceLevel());
        job.setLastDate(jobDetails.getLastDate());
        job.setDescription(jobDetails.getDescription());
        job.setApplyLink(jobDetails.getApplyLink());
        job.setActive(jobDetails.isActive());
        job.setTags(jobDetails.getTags());

        return jobRepository.save(job);
    }

    public void deleteJob(String id) {
        Job job = getJobById(id);
        jobRepository.delete(job);
    }
}
