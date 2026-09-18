package com.smartexam.service;

import com.smartexam.dto.AnalyticsDto;
import com.smartexam.dto.LeaderboardDto;
import com.smartexam.entity.User;

import java.util.List;

public interface AnalyticsService {
    AnalyticsDto getSystemAnalytics();
    List<LeaderboardDto> getGlobalLeaderboard();
    List<LeaderboardDto> getExamLeaderboard(Long examId);
}
