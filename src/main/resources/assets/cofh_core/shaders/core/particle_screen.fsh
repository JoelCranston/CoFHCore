#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
    float a = texColor.a * vertexColor.a * ColorModulator.a;
    if (a < 0.0039) {
        discard;
    }
    vec4 color = vec4((1.0 - (1.0 - texColor.rgb) * (1.0 - vertexColor.rgb)) * ColorModulator.rgb, a);
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
