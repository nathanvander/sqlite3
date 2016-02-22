package apollo;
/**
* The sqlite code doesn't throw any exceptions, instead it returns error codes.  In general, I think it is a better way to program.
* However, some methods do throw exceptions, and when they do, this is what to throw.
*
* This is the equivalent of the java.sql.SQLException that we all hate.
*
* This code is stolen directly from com.almworks.sqlite4java.SQLiteException, and so their license applies.
* =====================
*
*  * Copyright 2010 ALM Works Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
* =====================
*/

public class SQLiteException extends Exception {
  private final int myErrorCode;

  /**
   * Creates an instance of SQLiteException.
   *
   * @param errorCode
   * @param errorMessage optional error message
   */
  public SQLiteException(int errorCode, String errorMessage) {
    this(errorCode, errorMessage, null);
  }

  /**
   * Creates an instance of SQLiteException.
   *
   * @param errorCode
   * @param errorMessage optional error message
   * @param cause error cause
   */
  public SQLiteException(int errorCode, String errorMessage, Throwable cause) {
    super("[" + errorCode + "] " + (errorMessage == null ? "sqlite error" : errorMessage), cause);
    myErrorCode = errorCode;
    //if (Internal.isFineLogging()) {
    //  Internal.logFine(getClass(), getMessage());
    //}
  }

  /**
   * Gets the error code returned by SQLite.
   *
   * @return error code
   */
  public int getErrorCode() {
    return myErrorCode;
  }

  /**
   * Gets base error code returned by SQLite. Base error code is the lowest 8 bit from the extended error code,
   * like SQLITE_IOERR_BLOCKED.
   *
   * @return error code
   */
  public int getBaseErrorCode() {
    return myErrorCode >= 0 ? myErrorCode & 0xFF : myErrorCode;
  }
}