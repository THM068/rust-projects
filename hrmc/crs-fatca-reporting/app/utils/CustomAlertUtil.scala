/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package utils

import models.xml.FileErrorCode.{fileErrorCodesForProblemStatus, CustomError as FileCustomError}
import models.xml.RecordErrorCode.{CustomError, DocRefIDFormat}
import models.xml.{FileErrorCode, FileErrors, RecordError, RecordErrorCode, ValidationErrors}
import play.api.Logging
import utils.ErrorDetails.{errorList, error_details_910}

object CustomAlertUtil extends Logging {

  private val expectedFileErrorCode                 = FileErrorCode.values.map(_.code).toSeq
  private val expectedRecordErrorCode               = RecordErrorCode.values.map(_.code).toSeq
  private val problemsStatusErrorCodes: Seq[String] = fileErrorCodesForProblemStatus.map(_.code).:+(DocRefIDFormat.code)

  def alertForProblemStatus(errors: ValidationErrors): Unit =
    if (alertFileError(errors.fileError) || alertRecordError(errors.recordError))
      logger.warn("File Rejected with unexpected error")

  private def alertFileError(fileErrors: Option[Seq[FileErrors]]): Boolean =
    fileErrors.exists { fileErrors =>
      fileErrors.filter(err => !expectedFileErrorCode.contains(err.code.code)).exists { err =>
        problemsStatusErrorCodes.contains(err) ||
        !isCustomFileError(err)
      }
    }

  private def isCustomFileError(fileErr: FileErrors): Boolean =
    fileErr.code == FileCustomError && fileErr.details.getOrElse("").contains(error_details_910)

  private def alertRecordError(recordErrors: Option[Seq[RecordError]]): Boolean =
    recordErrors.exists { recordError =>
      recordError.filter(err => !expectedRecordErrorCode.contains(err.code.code)).exists { err =>
        problemsStatusErrorCodes.contains(err) ||
        !isCustomRecordError(err)
      }
    }

  private def isCustomRecordError(err: RecordError): Boolean =
    err.code == CustomError && errorList.exists(err.details.getOrElse("").contains(_))

}
