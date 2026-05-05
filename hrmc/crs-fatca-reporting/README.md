
# crs-fatca-reporting

Backend service to support file uploads from crs & fatca.

The frontend to this service can be found [here](https://github.com/hmrc/crs-fatca-reporting-frontend)

---

### Running the service

Service manager: CRS_FATCA_ALL

**Port:** 10037

---
/validate-submission
### API

| Task                     | Supported methods | Description                                                |
|--------------------------|-------------------|------------------------------------------------------------|
| /callback                | POST              | Provides an endpoint for Upscan to reach after file upload |
| /upscan/details:uploadId | GET               | Uses the upload ID to find the details of the upload       |
| /upscan/status:uploadId  | GET               | Uses the upload ID to find the status of the upload        |
| /upscan/upload           | POST              | Requests an upload using Upscan                            |
| /validate-submission     | POST              | Validate file upload                                       |
---
### Upload sequence diagram

```mermaid
---
config:
      theme: redux
---
sequenceDiagram
    participant Frontend
    participant Backend
    participant Upscan
    participant EIS_CRS as EIS CRS
    participant EIS_FATCA as EIS FATCA
    participant SDES

    Frontend ->> Backend: Submit report request

    activate Backend
    Note over Backend: Determine file size

   alt Large file
        Backend ->> SDES: Send notification request
        SDES -->> Backend: Status callback (async)

    else Small file
        Backend ->> Upscan: Download file
        Upscan -->> Backend: File content

        Note over Backend: Inspect file<br/>Extract required data<br/>Build request payload

        alt CRS file
            Backend ->> EIS_CRS: Submit CRS payload
            EIS_CRS -->> Backend: Callback response
        else FATCA file
            Backend ->> EIS_FATCA: Submit FATCA payload
            EIS_FATCA -->> Backend: Callback response
        end
    end

    Backend -->> Frontend: Immediate response
    deactivate Backend
```


---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
