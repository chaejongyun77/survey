package com.woongjin.survey.domain.noti;

@Service
@RequiredArgsConstructor
public class NotificationHistoryQueryService {

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final SurveyQueryService surveyQueryService;

    public Page<NotificationHistoryListQueryResponse> getHistories(NotificationHistoryListQueryRequest req) {
        Page<NotificationHistory> histories = notificationHistoryRepository.findAll(req.toPageable());
        List<Long> surveyIds = histories.stream().map(NotificationHistory::getReferenceId).toList();

        Map<Long, SurveySubjectProjection> surveySubjectMap = surveyQueryService.getSurveySubjectMapByIds(surveyIds);

        return histories.map(it ->
                NotificationHistoryListQueryResponse.of(it, surveySubjectMap.get(it.getReferenceId())));
    }
}