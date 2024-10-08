#version 330

layout(location = 0) in vec4 position;

layout(std140) uniform GlobalMatrices {
    uniform mat4 cameraToClipMatrix;
    uniform mat4 worldToCameraMatrix;
};

uniform mat4 modelToWorldMatrix;

void main()
{
    vec4 temp = modelToWorldMatrix * position;
    temp = worldToCameraMatrix * temp;
    gl_Position = cameraToClipMatrix * temp;
}
