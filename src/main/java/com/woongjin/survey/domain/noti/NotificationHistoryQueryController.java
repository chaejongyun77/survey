/*
package com.woongjin.survey.domain.noti;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/external/v1/admin")
public class NotificationHistoryQueryController {

    private final NotificationHistoryQueryService historyQueryService;

    */
/**
     * 알림 발송 이력 조회
     *//*

    @GetMapping("/notifications/histories")
    public ApiResponse<PageQueryResponse<NotificationHistoryListQueryResponse>> getHistories(
            NotificationHistoryListQueryRequest req,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        Long empId = loginUser.getEmpId();

        Page<NotificationHistoryListQueryResponse> histories =
                historyQueryService.getHistories(req, empId);

        return ApiResponse.ok(new PageQueryResponse<>(histories));
    }
}*/
