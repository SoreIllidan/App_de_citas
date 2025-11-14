<?php
function send_push(array $tokens, string $title, string $body, array $data = []): array {
    $tokens = array_values(array_filter(array_unique($tokens)));
    if (empty($tokens)) return ["ok"=>0,"fail"=>0];

    // PON AQUÍ TU SERVER KEY DE FCM
    $SERVER_KEY = 'AAAA_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx';

    $payload = [
        'registration_ids' => $tokens,
        'notification' => [
            'title' => $title,
            'body'  => $body,
            'sound' => 'default'
        ],
        'data' => $data,
        'android' => [
            'priority' => 'high'
        ]
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Authorization: key=' . $SERVER_KEY,
        'Content-Type: application/json'
    ]);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
    $resp = curl_exec($ch);
    $http = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($http !== 200) return ["ok"=>0,"fail"=>count($tokens),"http"=>$http];

    $j = json_decode($resp, true);
    return ["ok"=>($j['success'] ?? 0), "fail"=>($j['failure'] ?? 0)];
}