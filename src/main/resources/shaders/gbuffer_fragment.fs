#version 330

const int NUM_CASCADES = 3;

in float vs_hovered;
in float vs_selected;
in float vs_highlighted;
in vec3  vs_worldpos;
in vec2  vs_textcoord;
in vec2  vs_bordercoord;
in vec3  vs_normal;
in vec4  vs_mlightviewVertexPos[NUM_CASCADES];
in mat4  vs_modelMatrix;
in vec4  vs_mvVertexPos;
in vec4  vs_surroundings;
in vec4  vs_surroundingsDiag;

layout (location = 0) out vec3 fs_worldpos;
layout (location = 1) out vec3 fs_diffuse;
layout (location = 2) out vec3 fs_specular;
layout (location = 3) out vec3 fs_normal;
layout (location = 4) out vec2 fs_shadow;

struct Material
{
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    int hasNormalMap;
    float reflectance;
};

uniform sampler2D texture_sampler;
uniform sampler2D normalMap;
uniform sampler2D texture_border;
uniform Material  material;

uniform sampler2D shadowMap_0;
uniform sampler2D shadowMap_1;
uniform sampler2D shadowMap_2;
uniform float cascadeFarPlanes[NUM_CASCADES];
uniform mat4 orthoProjectionMatrix[NUM_CASCADES];
uniform int renderShadow;
uniform int renderBorder;
uniform int renderTile;
uniform int renderAmbiantOcclusion;

vec3 diffuseC;
vec3 speculrC;

float getEaseIn(float x){
	float p = mod(x, 1) - 1;
	return p*p*p + 1;
	
}

float getEaseInDiag(float x, float y){
	float p = mod(x, 1) + mod(y, 1);
	return p >= 1 ? 1 : getEaseIn(p);
}

vec3 calcAmbiantOcclusion(vec3 diffuseC, float normal, float pos1, float pos2){
	if(normal != 0) {
		if(vs_surroundings.x > 0){
			diffuseC = diffuseC * getEaseIn(pos1);
		}
		if(vs_surroundings.y > 0){
			diffuseC = diffuseC * getEaseIn(pos2);
		}
		if(vs_surroundings.z > 0){
			diffuseC = diffuseC * getEaseIn( 1 - pos1);
		}
		if(vs_surroundings.w > 0){
			diffuseC = diffuseC * getEaseIn( 1 - pos2);
		}
		
		if(vs_surroundingsDiag.x > 0 && vs_surroundings.x == 0 && vs_surroundings.y == 0 ){
			diffuseC = diffuseC * getEaseInDiag(pos1, pos2);
		}
		if(vs_surroundingsDiag.y > 0 && vs_surroundings.w == 0 && vs_surroundings.x == 0){
			diffuseC = diffuseC * getEaseInDiag(pos1, 1 - pos2);
		}
		if(vs_surroundingsDiag.z > 0 && vs_surroundings.z == 0 && vs_surroundings.w == 0){
			diffuseC = diffuseC * getEaseInDiag(1 - pos1, 1 - pos2);
		}
		if(vs_surroundingsDiag.w > 0 && vs_surroundings.y == 0 && vs_surroundings.z == 0){
			diffuseC = diffuseC * getEaseInDiag(1 - pos1, pos2);
		}
	}
	return diffuseC;
}

void getColour(Material material, vec2 textCoord)
{
    if (material.hasTexture == 1)
    {
        diffuseC = texture(texture_sampler, textCoord).xyz;
    	speculrC = diffuseC;
    } 
    else
    {
        diffuseC = material.diffuse.xyz;
        speculrC = material.specular.xyz;
    }
    if(renderTile == 1 && vs_normal.y > 0){
    	if (vs_selected > 0) {
	        diffuseC = vec3(1, diffuseC.y, diffuseC.z);
	    } else if (vs_hovered > 0) {
	        diffuseC = vec3(diffuseC.x, diffuseC.y, 1);
	    } else if (vs_highlighted > 0) {
	        diffuseC = vec3(diffuseC.x, 1,diffuseC.z);
	    }
	    float modX = mod(vs_worldpos.x, 8);
	    float modZ = mod(vs_worldpos.z, 8);
	    if(modZ > 7.6){
	    	modZ = 8 - modZ;
	    	diffuseC = diffuseC * vec3(modZ * 2.5, modZ * 2.5, modZ * 2.5);
	        speculrC = speculrC * vec3(modZ * 2.5, modZ * 2.5, modZ * 2.5);
	    }
	    if(modX < 0.4){
	    	diffuseC = diffuseC * vec3(modX * 2.5, modX * 2.5, modX * 2.5);
	        speculrC = speculrC * vec3(modX * 2.5, modX * 2.5, modX * 2.5);
	    }
	    if(modZ < 0.4){
	    	diffuseC = diffuseC * vec3(modZ * 2.5, modZ * 2.5, modZ * 2.5);
	        speculrC = speculrC * vec3(modZ * 2.5, modZ * 2.5, modZ * 2.5);
	    }
	    if(modX > 7.6){
	    	modX = 8 - modX;
	    	diffuseC = diffuseC * vec3(modX * 2.5, modX * 2.5, modX * 2.5);
	        speculrC = speculrC * vec3(modX * 2.5, modX * 2.5, modX * 2.5);
		}
    }
    
	if(renderAmbiantOcclusion == 1){
		diffuseC = calcAmbiantOcclusion(diffuseC, vs_normal.x, vs_worldpos.y, vs_worldpos.z);
		diffuseC = calcAmbiantOcclusion(diffuseC, vs_normal.y, vs_worldpos.x, vs_worldpos.z);
		diffuseC = calcAmbiantOcclusion(diffuseC, vs_normal.z, vs_worldpos.x, vs_worldpos.y);
	}
	if(renderBorder == 1){
		diffuseC = diffuseC * texture(texture_border, vs_bordercoord).xyz;
		speculrC = speculrC * texture(texture_border, vs_bordercoord).xyz;
	}
}

vec3 calcNormal(Material material, vec3 normal, vec2 text_coord, mat4 modelMatrix)
{
    vec3 newNormal = normal;
    if ( material.hasNormalMap == 1 )
    {
        newNormal = texture(normalMap, text_coord).rgb;
        newNormal = normalize(newNormal * 2 - 1);
        newNormal = normalize(modelMatrix * vec4(newNormal, 0.0)).xyz;
    }
    return newNormal;
}

float calcShadow(vec4 position, int idx)
{
    if ( renderShadow == 0 )
    {
        return 1.0;
    }

    vec3 projCoords = position.xyz;
    // Transform from screen coordinates to texture coordinates
    projCoords = projCoords * 0.5 + 0.5;
    float bias = 0.005;

    float shadowFactor = 0.0;
    vec2 inc;
    if (idx == 0)
    {
        inc = 1.0 / textureSize(shadowMap_0, 0);
    }
    else if (idx == 1)
    {
        inc = 1.0 / textureSize(shadowMap_1, 0);
    }
    else
    {
        inc = 1.0 / textureSize(shadowMap_2, 0);
    }
    for(int row = -1; row <= 1; ++row)
    {
        for(int col = -1; col <= 1; ++col)
        {
            float textDepth;
            if (idx == 0)
            {
                textDepth = texture(shadowMap_0, projCoords.xy + vec2(row, col) * inc).r; 
            }
            else if (idx == 1)
            {
                textDepth = texture(shadowMap_1, projCoords.xy + vec2(row, col) * inc).r; 
            }
            else
            {
                textDepth = texture(shadowMap_2, projCoords.xy + vec2(row, col) * inc).r; 
            }
            shadowFactor += projCoords.z - bias > textDepth ? 1.0 : 0.0;        
        }    
    }
    shadowFactor /= 9.0;

    if(projCoords.z > 1.0)
    {
        shadowFactor = 1.0;
    }

    return 1 - shadowFactor;
} 

void main()
{
    getColour(material, vs_textcoord);
    
    fs_worldpos   = vs_worldpos;
    
    fs_diffuse    = diffuseC;
    fs_specular   = speculrC;
    fs_normal     = normalize(calcNormal(material, vs_normal, vs_textcoord, vs_modelMatrix));

    int idx;
    for (int i=0; i<NUM_CASCADES; i++)
    {
        if ( abs(vs_mvVertexPos.z) < cascadeFarPlanes[i] )
        {
            idx = i;
            break;
        }
    }
	fs_shadow  = vec2(calcShadow(vs_mlightviewVertexPos[idx], idx), material.reflectance);
}