# Render PlantUML diagrams with AWS Lambda

A serverless UI + API to render [PlantUML](http://plantuml.com) diagrams.

## NOTE Feburary 2026 update

There are ongoing maintainance issues with this project. The security of the app isn't a problem, due to the way we invocation isolate it in AWS lambda. The functionality issues are:

- PlantUML version is dated: v1.2023.10 is over 2 years old.
- Java 11 EOL: Running on Java 11 (Amazon Corretto), which is approaching end of extended support.
- Node.js 14 in build image: build-image/Dockerfile installs Node 14, which has been EOL since April 2023.
- Network include are blocked: !include https://..., !include_once, !include_many with HTTP URLs are detected by regex in PlantUmlUtil.java:33-34 and have caching disabled (return NOETAG). Local file includes won't work either — Lambda's filesystem is read-only/sandboxed
- Fonts are severely limited:
    Only unifont-14.0.03.ttf is bundled (a minimal Unicode bitmap font)
    No DejaVu, Liberation, monospace families, or any other typefaces
    Standard server has 10+ font families
- Only PNG, SVG, TXT are implemented, no PDF, EPS, Base64, VDXL, XMI, SCXML, or LaTeX output
- No image map generation
- Outdated graphviz, 2.42.3, from 2019.
- Doesn't support Stdlib (C4, AWS Icons, etc.)

Rather than updating, the most likely route forward is a direct PlantUML to draw.io convertor in JS. See https://github.com/jgraph/plantuml-converter

## Drop in replacement for official PlantUML server

This can be used as a drop in replacement for scenarios
where http://www.plantuml.com/plantuml is used as a rendering endpoint. You can avoid sending the diagram source to a server outside your control and use an encrypted HTTPS endpoint for the diagram traffic.

This doesn't support everything the official PlantUML server does but should be good for most intents and purposes (PNG, SVG and TXT rendering).

For example, to have Visual Studio Code PlantUML plugin render using your own serverless deployment, set the following properties in vscode for the plugin: 

```json
    "plantuml.render": "PlantUMLServer",
    "plantuml.server": "https://your-endpoint-here"
```

Use `"plantuml.server": "https://plantuml.nitorio.us"` if you'd like to try before you deploy your own.

## Demo

### PNG Diagram

![Rendered PNG diagram should be here](https://plantuml.nitorio.us/png/Kt8goYylJYrIKj2rKr1o3F1KS4yiIIrFh5IoKWZ8ALOeIirBIIrIACd8B5Oeo4dCAodDpU52KGVMw9EOcvIIgE1McfTSafcVfwI0JpU6Of09C6czhCGYlDgnwBVHrSKq80YiEJL58IKpCRqeCHVDrM0zM9oDgGqUGc0jg464hXe0)

### SVG Diagram 

![Rendered SVG diagram should be here](https://plantuml.nitorio.us/svg/Kt8goYylJYrIKj2rKr1o3F1KS4yiIIrFh5IoKWZ8ALOeIirBIIrIACd8B5Oeo4dCAodDpU52KGVMw9EOcvIIgE1McfTSafcVfwI0JpU6Of09C6czhCGYlDgnwBVHrSKq80YiEJL58IKpCRqeCHVDrM0zM9oDgGqUGc0jg464hXe0)

### TXT Diagram

https://plantuml.nitorio.us/txt/Kt8goYylJYrIKj2rKr1o3F1KS4yiIIrFh5IoKWZ8ALOeIirBIIrIACd8B5Oeo4dCAodDpU52KGVMw9EOcvIIgE1McfTSafcVfwI0JpU6Of09C6czhCGYlDgnwBVHrSKq80YiEJL58IKpCRqeCHVDrM0zM9oDgGqUGc0jg464hXe0

## Build

- `npm ci`
- `mvn clean package`

## Deploy

You can deploy with Serverless framework or AWS SAM.

This used to be available on the AWS Serverless Application Repository, but currently that's not possible because it
doesn't appear to support lambda functions packaged as container images.

### Serverless framework:

- Edit `serverless.yml` to replace `custom.domains.dev` and `custom.domains.prod` with your own domain names.
    * If you don't want a custom domain name, remove or comment out the `serverless-domain-manager` plugin from the plugins list and skip
      the `sls create-cert` and `sls create_domain` commands.
- Run `sls create-cert` to create an ACM certificate for your domain as configured in `custom.customCertificate`.
- Run `sls create_domain` to create an API Gateway custom domain as configured in `custom.customDomain`.
- Run `sls deploy`.

The above steps deploy the default `dev` stage. To deploy the `prod` stage, add `--stage prod` to each command.

### AWS SAM

- Run `sam-deploy.sh`

The SAM deployment doesn't include custom domains currently.

### JGraph deployment

- Set the region in the `serverless.yml` file, then deploy using `sls deploy`
- Ensure gateway urls are the same and matches the ones used in region CF worker
- Ensure the gateway custom domain (plant-aws.diagrams.net) API mapping is correct. Currently, it points to `prod-plantuml-serverless` - `prod` in `eu-central-1` region
- You can edit the domain manually also from (API Gateway -> Custom domain names) and set the new stack name (API Mapping)
