<?php
$host = "localhost";
$user = "root";
$pass = "";
$db = "univalut_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Connection failed"]);
    exit();
}

$student_number = $conn->real_escape_string($student_number);
$password = $conn->real_escape_string($password);

$sql = "SELECT * FROM students_new WHERE student_number = '$student_number' AND password = '$password' LIMIT 1";
$result = $conn->query($sql);

if ($result && $result->num_rows > 0) {
     $row = $result->fetch_assoc();
     if($row['verified'] == 1){
        echo json_encode(["success" => true, "user_type" => "student"]);
     }
    else{
    echo json_encode(["success" => false, "message" => "check ur spam box to verify your mail"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Invalid student credentials"]);
}

$conn->close();
