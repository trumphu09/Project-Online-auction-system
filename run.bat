@echo off
echo === STARTING AUCTION SYSTEM ===

echo Starting Server...
start "Auction Server" cmd /k "cd Server && java -cp "target\classes;lib/*" com.auction.server.AuctionServer"

timeout /t 3 /nobreak > nul

echo Starting Client...
start "Auction Client" cmd /k "cd Client && java --module-path "lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes;lib/*" org.example.MainApp"

echo System started! Close this window to stop all services.
pause