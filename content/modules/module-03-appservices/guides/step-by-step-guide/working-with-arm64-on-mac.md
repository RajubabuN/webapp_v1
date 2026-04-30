# Working with ARM64 on Mac for Azure App Service (Platform Mismatch Fix)

> ⚠️ **Disclaimer**: If you’re using a Mac with an M1/M2 chip — welcome to the world of platform mismatch. Azure App Service doesn’t care that you’re cutting-edge. It still wants `linux/amd64`. And yes, this guide might become outdated before your next brew finishes ☕. When in doubt — emu it, buildx it, or ask ChatGPT to explain it like you're five.

<hr>

## The problem

Azure App Service (as of 2025) **does not support ARM64 (`linux/arm64`) containers**.  
It only supports `linux/amd64` (also called x86-64) architecture for Web App containers.

If you're using a Mac with an M1 or M2 chip, Docker on your machine will **by default** try to build images for `linux/arm64`. This leads to images that **do not run on Azure App Service**, causing startup failure or "Application Error" in the browser.

---

## Solution: build for AMD64, even on ARM64 Mac

There are several ways to build or convert your container images so they are compatible with Azure App Service.

---

### Option 1: Build multi-architecture images using Docker Buildx

This is the most flexible and recommended way. You can build one image that supports both ARM64 and AMD64 platforms:

```bash
docker buildx build --platform linux/amd64,linux/arm64 \
  -t <acr-name>.azurecr.io/petstore-app:latest \
  --push .
```

This pushes a **multi-arch manifest** to your ACR registry.

To verify:

```bash
docker manifest inspect <acr-name>.azurecr.io/petstore-app:latest
```

> App Service will automatically pull the `amd64` variant (since it's the only one it supports).

---

### Option 2: Push an AMD64-only image and use that tag in App Service

You can build and tag a specific AMD64 image locally like this:

```bash
docker build --platform linux/amd64 -t petstore-app:amd64 .
docker tag petstore-app:amd64 <acr-name>.azurecr.io/petstore-app:amd64
docker push <acr-name>.azurecr.io/petstore-app:amd64
```

Then, in Azure App Service → **Deployment Center**, set the image tag to `petstore-app:amd64`.

> This avoids multi-arch but gives full control over what Azure runs.

---

### Option 3: Use emulation to build AMD64 locally

Docker for Mac supports platform emulation using QEMU.

You can enable `buildx` and build an AMD64 image, even on your ARM64 laptop:

```bash
docker buildx create --use
docker buildx build --platform linux/amd64 -t <acr-name>.azurecr.io/petstore-app:latest --push .
```

No need to switch base images or use ARM-specific tools.

> It’s a bit slower but works reliably.

---

### Option 4: Adjust your Dockerfile for cross-platform builds

Use platform-aware Docker syntax to make sure you build for the right target architecture.

Example:

```Dockerfile
# First stage (build)
FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jre-alpine AS build
# build steps here...

# Second stage (runtime)
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine
COPY --from=build /app /app
```

> This ensures the runtime image is always built for `amd64`, even if the build stage adapts to your current machine.

---

### Option 5: Build AMD64 in the cloud using ACR Tasks

Let Azure build for you — this avoids platform mismatches on your local machine entirely.

Create an `acr-task.yaml` file:

```yaml
version: v1.1.0
steps:
  - build: -t {{.Run.Registry}}/petstore-app:{{.Run.ID}}-amd64 --platform linux/amd64 -f Dockerfile .
  - push: ["{{.Run.Registry}}/petstore-app:{{.Run.ID}}-amd64"]
```

Then run:

```bash
az acr run -f acr-task.yaml .
```

> This is the best option if your local setup has too many conflicts.

---

## Final notes

- You **do not need to switch base images** (e.g. from `temurin` to `corretto`) just because of platform differences.
- You **do not need to configure anything special** in ACR or App Service — just make sure your image is built for the correct architecture (`linux/amd64`).
- Multi-arch images are useful if you want the **same image to work** both in Azure and locally.

---

## References

- [1] [Push multi-arch images to ACR (Microsoft)](https://learn.microsoft.com/en-us/azure/container-registry/push-multi-architecture-images)
- [2] [Multi-arch builds with Docker Buildx (paulyu.dev)](https://paulyu.dev/article/building-and-deploying-multi-arch-container-images-guide/)
- [3] [Azure App Service custom containers](https://learn.microsoft.com/en-us/azure/app-service/tutorial-custom-container)
- [4] [StackOverflow: ARM64 image not working in App Service](https://stackoverflow.com/questions/73621200/how-to-host-a-arm64-docker-container-on-azure-container-apps)
- [5] [Build multi-arch images to ACR (Alibaba Cloud example)](https://www.alibabacloud.com/help/en/acr/use-cases/build-and-push-multi-arch-images-locally-to-container-registry-service)
