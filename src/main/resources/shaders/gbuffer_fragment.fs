#version 330

const int NUM_CASCADES = 3;

in vec3  vs_worldpos;
in vec2  vs_textcoord;
in vec2  vs_bordercoord;
in vec3  vs_normal;
in vec4  vs_mlightviewVertexPos[NUM_CASCADES];
in mat4  vs_modelMatrix;
in vec4  vs_mvVertexPos;

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
uniform sampler2D texture_border_left;
uniform sampler2D texture_border_top;
uniform sampler2D texture_border_right;
uniform sampler2D texture_border_bottom;
uniform Material  material;

uniform sampler2D shadowMap_0;
uniform sampler2D shadowMap_1;
uniform sampler2D shadowMap_2;
uniform float cascadeFarPlanes[NUM_CASCADES];
uniform mat4 orthoProjectionMatrix[NUM_CASCADES];
uniform int renderShadow;
uniform int renderBorder;

uniform vec3 selectedBlocks[100];
uniform vec2 mousePosition;

vec4 diffuseC;
vec4 speculrC;

void getColour(Material material, vec2 textCoord)
{
    if (material.hasTexture == 1)
    {
        diffuseC = texture(texture_sampler, textCoord);
    	speculrC = diffuseC;
    } 
    else
    {
        diffuseC = material.diffuse;
        speculrC = material.specular;
    }
    if(renderBorder == 1){
        if(vs_normal.y != 0) {
        	if(vs_normal.y >0) {
		        for(int i = 0; i < selectedBlocks.length; ++i)
		    	{
		    		vec3 selectedBlock = selectedBlocks[i];
		    		if(vs_worldpos.x > (selectedBlock.x * 8) && vs_worldpos.x < (((selectedBlock.x + 1) * 8)+0.01)
		    			&& vs_worldpos.y >= ((selectedBlock.y * 8)+0.01) && vs_worldpos.y < (((selectedBlock.y + 1) * 8)+0.01) 
		    			&& vs_worldpos.z >= ((selectedBlock.z * 8)+0.01) && vs_worldpos.z < (((selectedBlock.z + 1) * 8)+0.01)) { 
				    	diffuseC = vec4(1, 0, 0, 1);
				        speculrC = vec4(1, 0, 0, 1);
				        break;
			        }
		    	}
		    }
	        if(mod(vs_worldpos.z, 8) > 7.8) {
		    	diffuseC = vec4(0,0,0,1);
		        speculrC = vec4(0,0,0,1);
			}
			if(mod(vs_worldpos.x, 8) < 0.2) {
		    	diffuseC = vec4(0,0,0,1);
		        speculrC = vec4(0,0,0,1);
			}
			if(mod(vs_worldpos.z, 8) < 0.2) {
		    	diffuseC = vec4(0,0,0,1);
		        speculrC = vec4(0,0,0,1);
			}
			if(mod(vs_worldpos.x, 8) > 7.8) {
		    	diffuseC = vec4(0,0,0,1);
		        speculrC = vec4(0,0,0,1);
			}
		}
    	
		diffuseC = diffuseC * texture(texture_border, vs_bordercoord);
		speculrC = speculrC * texture(texture_border, vs_bordercoord);
		
        if(renderBorder == 0){
	        if(mod(vs_worldpos.z, 8) > 7) {
		        diffuseC = diffuseC * texture(texture_border_left, vs_bordercoord);
		        speculrC = speculrC * texture(texture_border_left, vs_bordercoord);
			}
			if(mod(vs_worldpos.x, 8) < 1) {
		        diffuseC = diffuseC * texture(texture_border_top, vs_bordercoord);
		        speculrC = speculrC * texture(texture_border_top, vs_bordercoord);
			}
			if(mod(vs_worldpos.z, 8) < 1) {
		        diffuseC = diffuseC * texture(texture_border_right, vs_bordercoord);
		        speculrC = speculrC * texture(texture_border_right, vs_bordercoord);
			}
			if(mod(vs_worldpos.x, 8) > 7) {
		        diffuseC = diffuseC * texture(texture_border_bottom, vs_bordercoord);
		        speculrC = speculrC * texture(texture_border_bottom, vs_bordercoord);
			}
     	}
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
    
    fs_diffuse    = diffuseC.xyz;
    fs_specular   = speculrC.xyz;
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