param(
    [string]$BaseUrl = "http://localhost:8080/api",
    [string]$CompanyName = "TestCompany"
)

$ErrorActionPreference = "Stop"

function To-JsonBody($obj) {
    $obj | ConvertTo-Json -Depth 6 -Compress
}

function Invoke-ApiJson {
    param(
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [string]$Token = ""
    )

    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $params = @{
        Method = $Method
        Uri = $Url
        Headers = $headers
        ContentType = "application/json"
    }

    if ($null -ne $Body) {
        $params["Body"] = To-JsonBody $Body
    }

    Invoke-RestMethod @params
}

function Login {
    param(
        [string]$Username,
        [string]$Password
    )

    $resp = Invoke-ApiJson -Method "POST" -Url "$BaseUrl/auth/login" -Body @{ username = $Username; password = $Password }
    if ($resp.code -ne 0 -or -not $resp.data.accessToken) {
        throw "Login failed for user ${Username}."
    }
    $resp.data.accessToken
}

Write-Host "[1/6] Login admin and prepare employees..."
$adminToken = Login -Username "admin" -Password "Admin@123"

$seedKey = Get-Date -Format yyyyMMddHHmmss
$employeeUser = "emp_seed_${seedKey}"
[void](Invoke-ApiJson -Method "POST" -Url "$BaseUrl/users" -Token $adminToken -Body @{
    username = $employeeUser
    password = "Emp@12345"
    nickname = "SeedEmployee"
    role = "EMPLOYEE"
    employedCompany = $CompanyName
})
$employeeToken = Login -Username $employeeUser -Password "Emp@12345"

Write-Host "[2/6] Seed work logs..."
$projects = @("Alpha", "Beta", "Gamma", "Delta")
$tasks = @("DEV", "TEST", "MEETING")
$priorities = @("LOW", "MEDIUM", "HIGH")

for ($i = 1; $i -le 36; $i++) {
    $dateObj = (Get-Date).AddDays(-($i % 12))
    $date = $dateObj.ToString("yyyy-MM-dd")
    $h = 9 + ($i % 3)
    $start = $date + "T" + $h.ToString("00") + ":00:00"
    $end = $date + "T" + ($h + 2).ToString("00") + ":30:00"

    $payload = @{
        workDate = $date
        title = "seed-log-" + $i
        content = "seed for pagination filter sort"
        projectName = $projects[$i % $projects.Length]
        taskType = $tasks[$i % $tasks.Length]
        priorityLevel = $priorities[$i % $priorities.Length]
        startTime = $start
        endTime = $end
        tags = "seed,case" + $i
    }

    [void](Invoke-ApiJson -Method "POST" -Url "$BaseUrl/logs/daily" -Body $payload -Token $employeeToken)
}

$logsResp = Invoke-ApiJson -Method "GET" -Url "$BaseUrl/logs" -Token $employeeToken
$logs = @($logsResp.data)

Write-Host "[3/6] Verify pagination/filter/sort..."
if ($logs.Count -lt 30) {
    throw "Expected >= 30 logs, got $($logs.Count)"
}

$project = "Alpha"
$filtered = @($logs | Where-Object { $_.projectName -eq $project })
if ($filtered.Count -eq 0) {
    throw "Filter check failed for project $project"
}

$sorted = @($logs | Sort-Object -Property @{ Expression = "workDate"; Descending = $true }, @{ Expression = "workHours"; Descending = $true })
$page3 = @($sorted | Select-Object -Skip 20 -First 10)
if ($page3.Count -eq 0) {
    throw "Pagination check failed: page 3 empty"
}
Write-Host "  PASS logs=$($logs.Count), filter($project)=$($filtered.Count), page3=$($page3.Count)"

Write-Host "[4/6] Verify employee review company and auto-approve..."
$submitResp = Invoke-ApiJson -Method "POST" -Url "$BaseUrl/reviews/submit" -Token $employeeToken -Body @{
    companyName = "WrongCompanyShouldBeIgnored"
    cultureScore = 4
    teamScore = 5
    growthScore = 4
    salaryScore = 3
    balanceScore = 4
    anonymousMode = $false
    publicVisible = $true
    reviewContent = "employee review seed"
}

$reviewId = $submitResp.data.id
if ($submitResp.data.reviewStatus -ne "APPROVED") {
    throw "Employee review status should be APPROVED"
}
if ($submitResp.data.companyName -ne $CompanyName) {
    throw "Employee review company mismatch. expected=$CompanyName actual=$($submitResp.data.companyName)"
}
Write-Host "  PASS review id=$reviewId company=$($submitResp.data.companyName) status=$($submitResp.data.reviewStatus)"

Write-Host "[5/6] Verify employee without company cannot submit review..."
$seedUser = "emp_nocomp_${seedKey}"
[void](Invoke-ApiJson -Method "POST" -Url "$BaseUrl/users" -Token $adminToken -Body @{
    username = $seedUser
    password = "Emp@12345"
    nickname = "NoCompany"
    role = "EMPLOYEE"
})

$nocompToken = Login -Username $seedUser -Password "Emp@12345"
$blockedResp = Invoke-ApiJson -Method "POST" -Url "$BaseUrl/reviews/submit" -Token $nocompToken -Body @{
    companyName = $CompanyName
    cultureScore = 4
    teamScore = 4
    growthScore = 4
    salaryScore = 4
    balanceScore = 4
    anonymousMode = $true
    publicVisible = $true
    reviewContent = "should fail"
}

if ($blockedResp.code -eq 0) {
    throw "Expected no-company employee submit to be blocked"
}
Write-Host "  PASS no-company employee submit is blocked"

Write-Host "[6/6] Verify admin can delete employee review..."
[void](Invoke-ApiJson -Method "DELETE" -Url "$BaseUrl/reviews/$reviewId" -Token $adminToken)
$afterDelete = Invoke-ApiJson -Method "GET" -Url "$BaseUrl/reviews/company/$CompanyName" -Token $adminToken
$stillExists = @($afterDelete.data | Where-Object { $_.id -eq $reviewId }).Count -gt 0
if ($stillExists) {
    throw "Review still exists after delete. id=$reviewId"
}
Write-Host "  PASS admin deleted review id=$reviewId"

Write-Host "All integration checks passed."
