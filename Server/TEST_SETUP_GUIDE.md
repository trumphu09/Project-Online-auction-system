# 🚀 SETUP & RUN DATABASE TESTS

## 📋 Prerequisite Check

```powershell
# Check Java version (should be 17+)
java -version

# Check if Maven exists
mvn -version
```

---

## 🔧 Option 1: Install Maven Automatically (Windows)

### Using Chocolatey
```powershell
# If you have Chocolatey installed:
choco install maven
```

### Using SCOOP
```powershell
scoop install maven
```

### Manual Install
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to: `C:\Server\apache-maven-3.9.x` (or any folder)
3. Add to Windows PATH:
   - Right-click "This PC" → Properties
   - → Advanced system settings
   - → Environment Variables
   - → New (System Variable):
     - Variable name: `MAVEN_HOME`
     - Variable value: `C:\Server\apache-maven-3.9.x`
   - Edit `PATH` and add: `%MAVEN_HOME%\bin`
4. Restart PowerShell/CMD
5. Verify: `mvn -version`

---

## 🧪 Option 2: Run Tests

### After Maven is installed:

**Option A: Using Maven (Recommended)**
```bash
cd C:\Users\admin\Documents\Project-Online-auction-system\Server
mvn test -Dtest=DatabaseOperationTest
```

**Option B: Using batch script**
```cmd
cd C:\Users\admin\Documents\Project-Online-auction-system\Server
run-tests.bat
```

**Option C: Using PowerShell**
```powershell
cd "C:\Users\admin\Documents\Project-Online-auction-system\Server"
mvn test
```

---

## 📊 What Tests Will Run

✅ 25 Comprehensive Tests:
- User Registration & Login
- Bidder Management
- Seller Management  
- Admin Functions
- Item Management
- Product Types (Electronics, Art, Vehicle)
- Bidding & Transactions
- Payment Processing
- Database Connectivity

---

## ✅ Expected Output

```
===== [TEST 1] User Registration with String Parameters =====
✓ User [testuser_xxxx] registered successfully

===== [TEST 2] User Login =====
✓ User logged in successfully with role: BIDDER

[... 23 more tests ...]

Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS ✅
```

---

## 🐛 Troubleshooting

**Problem: "mvn is not recognized"**
```
Solution: Maven not in PATH. Reinstall or add to PATH (see steps above)
```

**Problem: "Connection refused" (Database)**
```
Solution: Check Aiven database credentials in DatabaseConnection.java
Verify: URL, USERNAME, PASSWORD are correct
```

**Problem: "Tests Failed"**
```
Solution: 
1. Check database connection
2. Check if MySQL is running
3. Run: mvn clean compile
4. Run: mvn test again
```

---

## 📌 Quick Setup (One-liner for PowerShell)

If Maven is already installed:
```powershell
cd "C:\Users\admin\Documents\Project-Online-auction-system\Server"; mvn clean test -Dtest=DatabaseOperationTest
```

---

**Next Steps:**
1. Install Maven (if not done)
2. Verify: `mvn -version`
3. Run: `mvn test` in Server folder
4. Send screenshot of results ✅
