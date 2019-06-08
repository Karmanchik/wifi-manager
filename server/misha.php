<?php
  require_once 'connect.php';
  $metod = $_GET['action'];
  $link = mysqli_connect($host, $user, $password, $database);
  $result = array();

  if ($metod == "load") {
    $query = "INSERT INTO network(ssid, password, bssid) VALUES ('".$_GET['ssid']."','".$_GET['password']."','".$_GET['bssid']."')";
    mysqli_query($link, $query);
    $result = array('result' => 'true');
  }
  if ($metod == "download") {
    $query = "SELECT * FROM network";
    $answer = mysqli_query($link, $query);
    while ($row = mysqli_fetch_array($answer)) {
      $network = array(
        'ssid' => $row['ssid'],
        'password' => $row['password'],
        'bssid' => $row['bssid']
      );
      $result[] = $network;
    }
  }
  echo json_encode($result);
 ?>
