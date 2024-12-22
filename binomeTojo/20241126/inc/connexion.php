  <?php
function connexion()
{
    try {
        $DBH = new PDO("mysql:host=localhost;dbname=hexashop", "root", "");
        $DBH->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        return $DBH;
    } catch (PDOException $e) {
        echo $e->getMessage();
    }
}


//  function connexion()
//  {
//      try {
//          $DBH = new PDO("mysql:host=localhost;dbname=db_s2_ETU003191", "ETU003191", "8jxDpHko");
//          $DBH->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
//          return $DBH;
//      } catch (PDOException $e) {
//          echo $e->getMessage();
//      }
//  }
