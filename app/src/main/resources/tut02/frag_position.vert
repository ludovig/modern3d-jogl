#version 330

#define POSITION 0

layout (location = POSITION) in vec4 position;

void main()
{
    gl_Position = position;
}
